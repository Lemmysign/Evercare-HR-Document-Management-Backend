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
    private final ExecutorService executorService;

    public DocumentSubmissionServiceImpl(
            DocumentSubmissionRepository documentSubmissionRepository,
            StaffService staffService,
            DocumentRequirementService documentRequirementService,
            CloudinaryService cloudinaryService,
            DocumentSubmissionMapper documentSubmissionMapper,
            AuditService auditService) {

        this.documentSubmissionRepository = documentSubmissionRepository;
        this.staffService = staffService;
        this.documentRequirementService = documentRequirementService;
        this.cloudinaryService = cloudinaryService;
        this.documentSubmissionMapper = documentSubmissionMapper;
        this.auditService = auditService;

        // Thread pool for concurrent uploads
        this.executorService = new ThreadPoolExecutor(
                10,
                30,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(String staffIdNumber, DocumentUploadRequest request) {
        log.info("Processing document upload for staff: {}", staffIdNumber);

        // Validate file
        validateFile(request.getFile());

        // Find staff (case-insensitive)
        Staff staff = staffService.findByStaffIdNumber(staffIdNumber);

        // Verify staff has selected a department
        if (staff.getDepartment() == null) {
            throw new BadRequestException("Please select a department before uploading documents");
        }

        // Find document requirement
        DocumentRequirement requirement = documentRequirementService.findById(request.getDocumentRequirementId());

        // Verify requirement belongs to staff's department
        if (!requirement.getDepartment().getId().equals(staff.getDepartment().getId())) {
            throw new BadRequestException("This document requirement does not belong to your department");
        }

        // Check if already submitted
        if (documentSubmissionRepository.existsByStaffIdAndDocumentRequirementId(
                staff.getId(), requirement.getId())) {
            throw new BadRequestException("Document already submitted for this requirement");
        }

        // FIXED: Extract department name before Cloudinary upload
        String departmentName = staff.getDepartment().getName();
        String staffFullName = staff.getFullName();
        String documentName = requirement.getDocumentName();

        // Upload to Cloudinary
        try {
            String folder = cloudinaryService.generateFolder(departmentName);
            String customFileName = generateCustomFileName(staffFullName, documentName);

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(
                    request.getFile(),
                    folder,
                    customFileName
            );

            // Save submission to database
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


    @Transactional
    protected DocumentSubmission saveSubmissionInNewTransaction(DocumentSubmission submission) {
        return documentSubmissionRepository.save(submission);
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

        if (requirementIds.size() != files.size()) {
            throw new BadRequestException("Number of requirements must match number of files");
        }

        if (staff.getDepartment() == null) {
            throw new BadRequestException("Please select a department before uploading documents");
        }

        // CRITICAL FIX: Extract all lazy-loaded data BEFORE async operations
        String departmentName = staff.getDepartment().getName();
        UUID departmentId = staff.getDepartment().getId();
        String staffFullName = staff.getFullName();
        UUID staffId = staff.getId();
        String staffIdNum = staff.getStaffIdNumber();

        // Validate all files and requirements BEFORE starting async uploads
        List<DocumentRequirementData> requirementDataList = new ArrayList<>();

        for (int i = 0; i < requirementIds.size(); i++) {
            MultipartFile file = files.get(i);
            UUID requirementId = requirementIds.get(i);

            // Validate file
            validateFile(file);

            // Fetch and validate requirement
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
                        "Document already submitted"
                );
            }

            // Extract requirement data BEFORE async operation
            DocumentRequirementData reqData = new DocumentRequirementData(
                    requirement.getId(),
                    requirement.getDocumentName(),
                    requirement
            );
            requirementDataList.add(reqData);
        }

        // Now perform async uploads
        List<CompletableFuture<DocumentUploadResponse>> uploadFutures = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            final int index = i;
            final MultipartFile file = files.get(index);
            final DocumentRequirementData reqData = requirementDataList.get(index);

            CompletableFuture<DocumentUploadResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    log.info("Uploading document: {} for staff: {}", reqData.documentName, staffIdNum);

                    String folder = cloudinaryService.generateFolder(departmentName);
                    String customFileName = generateCustomFileName(staffFullName, reqData.documentName);

                    Map<String, Object> uploadResult = cloudinaryService.uploadFile(
                            file,
                            folder,
                            customFileName
                    );

                    // Create submission
                    DocumentSubmission submission = DocumentSubmission.builder()
                            .staff(staff)
                            .documentRequirement(reqData.requirement)
                            .cloudinaryUrl((String) uploadResult.get("secure_url"))
                            .cloudinaryPublicId((String) uploadResult.get("public_id"))
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .mimeType(file.getContentType())
                            .build();

                    // Save in new transaction
                    DocumentSubmission savedSubmission = saveSubmissionInNewTransaction(submission);

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
            }, executorService);

            uploadFutures.add(future);
        }

        try {
            CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                    uploadFutures.toArray(new CompletableFuture[0])
            );

            allUploads.get(300, TimeUnit.SECONDS); // 5 minutes timeout

            List<DocumentUploadResponse> responses = new ArrayList<>();
            for (CompletableFuture<DocumentUploadResponse> future : uploadFutures) {
                responses.add(future.get());
            }

            log.info("All documents uploaded successfully for staff: {}", staffIdNum);
            return responses;

        } catch (TimeoutException e) {
            log.error("Upload timeout for staff: {}", staffIdNum);
            throw new CloudinaryUploadException("Upload timeout. Please try again.");
        } catch (Exception e) {
            log.error("Failed to upload multiple documents for staff: {}", staffIdNum, e);
            throw new CloudinaryUploadException("Failed to upload documents: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countSubmissionsByStaffId(UUID staffId) {
        return documentSubmissionRepository.countByStaffId(staffId);
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
        if (input == null) return "";

        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

    // Helper class to store requirement data
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