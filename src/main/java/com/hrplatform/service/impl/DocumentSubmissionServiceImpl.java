package com.hrplatform.service.impl;

import com.hrplatform.dto.request.DocumentUploadRequest;
import com.hrplatform.dto.response.DocumentUploadResponse;
import com.hrplatform.entity.DocumentRequirement;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import com.hrplatform.exception.BadRequestException;
import com.hrplatform.exception.CloudinaryUploadException;
import com.hrplatform.exception.InvalidFileTypeException;
import com.hrplatform.mapper.DocumentSubmissionMapper;
import com.hrplatform.repository.DocumentSubmissionRepository;
import com.hrplatform.service.AuditService;
import com.hrplatform.service.CloudinaryService;
import com.hrplatform.service.DocumentRequirementService;
import com.hrplatform.service.DocumentSubmissionService;
import com.hrplatform.service.StaffService;
import com.hrplatform.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Slf4j
public class DocumentSubmissionServiceImpl implements DocumentSubmissionService {

    private final DocumentSubmissionRepository documentSubmissionRepository;
    private final StaffService staffService;
    private final DocumentRequirementService documentRequirementService;
    private final CloudinaryService cloudinaryService;
    private final DocumentSubmissionMapper documentSubmissionMapper;
    private final AuditService auditService;
    private final Executor taskExecutor;
    private final DocumentPersistenceService documentPersistenceService;

    // Manual constructor to handle @Qualifier properly
    public DocumentSubmissionServiceImpl(
            DocumentSubmissionRepository documentSubmissionRepository,
            StaffService staffService,
            DocumentRequirementService documentRequirementService,
            CloudinaryService cloudinaryService,
            DocumentSubmissionMapper documentSubmissionMapper,
            AuditService auditService,
            @Qualifier("taskExecutor") Executor taskExecutor, DocumentPersistenceService documentPersistenceService) {

        this.documentSubmissionRepository = documentSubmissionRepository;
        this.staffService = staffService;
        this.documentRequirementService = documentRequirementService;
        this.cloudinaryService = cloudinaryService;
        this.documentSubmissionMapper = documentSubmissionMapper;
        this.auditService = auditService;
        this.taskExecutor = taskExecutor;
        this.documentPersistenceService = documentPersistenceService;
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(String staffIdNumber, DocumentUploadRequest request) {
        log.info("Processing document upload for staff: {}", staffIdNumber);

        validateFile(request.getFile());

        Staff staff = staffService.findByStaffIdNumber(staffIdNumber);

        if (staff.getDepartment() == null) {
            throw new BadRequestException("Please select a department before uploading documents");
        }

        DocumentRequirement requirement = documentRequirementService.findById(request.getDocumentRequirementId());

        if (!requirement.getDepartment().getId().equals(staff.getDepartment().getId())) {
            throw new BadRequestException("This document requirement does not belong to your department");
        }

        if (documentSubmissionRepository.existsByStaffIdAndDocumentRequirementId(
                staff.getId(), requirement.getId())) {
            throw new BadRequestException("Document already submitted for this requirement");
        }

        String departmentName = staff.getDepartment().getName();
        String staffFullName = staff.getFullName();
        String documentName = requirement.getDocumentName();

        try {
            String folder = cloudinaryService.generateFolder(departmentName);
            String customFileName = generateCustomFileName(staffFullName, documentName);

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(
                    request.getFile(),
                    folder,
                    customFileName
            );

            DocumentSubmission submission = DocumentSubmission.builder()
                    .staff(staff)
                    .documentRequirement(requirement)
                    .cloudinaryUrl((String) uploadResult.get("secure_url"))
                    .cloudinaryPublicId((String) uploadResult.get("public_id"))
                    .fileName(request.getFile().getOriginalFilename())
                    .fileSize(request.getFile().getSize())
                    .mimeType(request.getFile().getContentType())
                    .build();

            DocumentSubmission savedSubmission = documentSubmissionRepository.save(submission);

            auditService.logUploadSuccess(
                    staff.getStaffIdNumber(),
                    departmentName,
                    documentName,
                    (String) uploadResult.get("secure_url")
            );

            log.info("Document uploaded successfully for staff: {}", staffIdNumber);

            return documentSubmissionMapper.toUploadResponse(
                    savedSubmission,
                    "Document uploaded successfully"
            );

        } catch (Exception e) {
            log.error("Failed to upload document for staff: {}", staffIdNumber, e);

            auditService.logUploadFailure(
                    staff.getStaffIdNumber(),
                    staff.getDepartment() != null ? staff.getDepartment().getName() : "UNKNOWN",
                    documentName,
                    e.getMessage()
            );

            throw new CloudinaryUploadException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countTotalSubmissions() {
        return documentSubmissionRepository.countAllSubmissions();
    }

    @Override
    @Transactional
    public List<DocumentUploadResponse> uploadMultipleDocumentsWithStaff(
            Staff staff,
            List<UUID> requirementIds,
            List<MultipartFile> files) {

        log.info("Processing multiple document uploads for staff: {} ({} files)",
                staff.getStaffIdNumber(), files.size());

        validateUploadRequest(requirementIds, files, staff);

        String departmentName = staff.getDepartment().getName();
        UUID departmentId = staff.getDepartment().getId();
        String staffFullName = staff.getFullName();
        UUID staffId = staff.getId();
        String staffIdNum = staff.getStaffIdNumber();

        List<DocumentRequirementData> requirementDataList =
                validateAndPrepareRequirements(requirementIds, files, departmentId, staffId);

        List<CompletableFuture<DocumentUploadResponse>> uploadFutures =
                processAsyncUploads(files, requirementDataList, staff, departmentName, staffFullName, staffIdNum);

        return waitForAllUploads(uploadFutures, staffIdNum);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countSubmissionsByStaffId(UUID staffId) {
        return documentSubmissionRepository.countByStaffId(staffId);
    }

    private void validateUploadRequest(List<UUID> requirementIds, List<MultipartFile> files, Staff staff) {
        if (requirementIds.size() != files.size()) {
            throw new BadRequestException("Number of requirements must match number of files");
        }

        if (staff.getDepartment() == null) {
            throw new BadRequestException("Please select a department before uploading documents");
        }
    }

    private List<DocumentRequirementData> validateAndPrepareRequirements(
            List<UUID> requirementIds,
            List<MultipartFile> files,
            UUID departmentId,
            UUID staffId) {

        List<DocumentRequirementData> requirementDataList = new ArrayList<>();

        for (int i = 0; i < requirementIds.size(); i++) {
            MultipartFile file = files.get(i);
            UUID requirementId = requirementIds.get(i);

            validateFile(file);

            DocumentRequirement requirement = documentRequirementService.findById(requirementId);

            if (!requirement.getDepartment().getId().equals(departmentId)) {
                throw new BadRequestException(
                        "Document requirement '" + requirement.getDocumentName() +
                                "' does not belong to your department"
                );
            }

            if (documentSubmissionRepository.existsByStaffIdAndDocumentRequirementId(
                    staffId, requirement.getId())) {
                throw new BadRequestException(
                        "Document '" + requirement.getDocumentName() + "' already submitted"
                );
            }

            requirementDataList.add(new DocumentRequirementData(
                    requirement.getId(),
                    requirement.getDocumentName(),
                    requirement
            ));
        }

        return requirementDataList;
    }

    private List<CompletableFuture<DocumentUploadResponse>> processAsyncUploads(
            List<MultipartFile> files,
            List<DocumentRequirementData> requirementDataList,
            Staff staff,
            String departmentName,
            String staffFullName,
            String staffIdNum) {

        List<CompletableFuture<DocumentUploadResponse>> uploadFutures = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            final MultipartFile file = files.get(i);
            final DocumentRequirementData reqData = requirementDataList.get(i);

            CompletableFuture<DocumentUploadResponse> future = CompletableFuture.supplyAsync(() ->
                            uploadSingleDocument(file, reqData, staff, departmentName, staffFullName, staffIdNum),
                    taskExecutor
            );

            uploadFutures.add(future);
        }

        return uploadFutures;
    }


    private DocumentUploadResponse uploadSingleDocument(
            MultipartFile file,
            DocumentRequirementData reqData,
            Staff staff,
            String departmentName,
            String staffFullName,
            String staffIdNum) {

        try {
            log.info("Uploading document: {} for staff: {}", reqData.documentName, staffIdNum);

            String folder = cloudinaryService.generateFolder(departmentName);
            String customFileName = generateCustomFileName(staffFullName, reqData.documentName);

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(
                    file,
                    folder,
                    customFileName
            );

            DocumentSubmission submission = DocumentSubmission.builder()
                    .staff(staff)
                    .documentRequirement(reqData.requirement)
                    .cloudinaryUrl((String) uploadResult.get("secure_url"))
                    .cloudinaryPublicId((String) uploadResult.get("public_id"))
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .build();

            DocumentSubmission savedSubmission = documentPersistenceService.saveInNewTransaction(submission);

            auditService.logUploadSuccess(
                    staffIdNum,
                    departmentName,
                    reqData.documentName,
                    (String) uploadResult.get("secure_url")
            );

            return documentSubmissionMapper.toUploadResponse(
                    savedSubmission,
                    "Document uploaded successfully"
            );

        } catch (Exception e) {
            log.error("Failed to upload document {} for staff: {}",
                    reqData.documentName, staffIdNum, e);

            auditService.logUploadFailure(
                    staffIdNum,
                    departmentName,
                    reqData.documentName,
                    e.getMessage()
            );

            throw new CloudinaryUploadException(
                    "Failed to upload " + reqData.documentName + ": " + e.getMessage()
            );
        }
    }

    private List<DocumentUploadResponse> waitForAllUploads(
            List<CompletableFuture<DocumentUploadResponse>> uploadFutures,
            String staffIdNum) {

        try {
            CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                    uploadFutures.toArray(new CompletableFuture[0])
            );

            allUploads.get(300, TimeUnit.SECONDS);

            List<DocumentUploadResponse> responses = new ArrayList<>();
            for (CompletableFuture<DocumentUploadResponse> future : uploadFutures) {
                responses.add(future.get());
            }

            log.info("All documents uploaded successfully for staff: {}", staffIdNum);
            return responses;

        } catch (TimeoutException e) {
            log.error("Upload timeout for staff: {}", staffIdNum);
            throw new CloudinaryUploadException("Upload timeout. Please try again.");
        } catch (ExecutionException e) {
            log.error("Failed to upload multiple documents for staff: {}", staffIdNum, e);
            Throwable cause = e.getCause();
            if (cause instanceof CloudinaryUploadException) {
                throw (CloudinaryUploadException) cause;
            }
            throw new CloudinaryUploadException("Failed to upload documents: " + cause.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Upload interrupted for staff: {}", staffIdNum);
            throw new CloudinaryUploadException("Upload interrupted. Please try again.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (!FileUtil.isValidFileType(file)) {
            throw new InvalidFileTypeException(
                    "Invalid file type. Allowed types: PDF, JPG, JPEG, PNG, DOC, DOCX"
            );
        }

        if (!FileUtil.isValidFileSize(file)) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum limit of %s", FileUtil.getMaxFileSizeFormatted())
            );
        }
    }

    private String generateCustomFileName(String staffName, String documentName) {
        String cleanStaffName = cleanString(staffName);
        String cleanDocName = cleanString(documentName);
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);

        return String.format("%s_%s_%s", cleanStaffName, cleanDocName, timestamp);
    }

    private String cleanString(String input) {
        if (input == null) {
            return "";
        }

        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

    private static class DocumentRequirementData {
        final UUID requirementId;
        final String documentName;
        final DocumentRequirement requirement;

        DocumentRequirementData(UUID requirementId, String documentName, DocumentRequirement requirement) {
            this.requirementId = requirementId;
            this.documentName = documentName;
            this.requirement = requirement;
        }
    }
}