package com.hrplatform.service;

import com.hrplatform.dto.request.DocumentUploadRequest;
import com.hrplatform.dto.response.DocumentUploadResponse;
import com.hrplatform.entity.Staff;
import com.hrplatform.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentSubmissionService {

    DocumentUploadResponse uploadDocument(String staffIdNumber, DocumentUploadRequest request) throws FileStorageException;


    Long countTotalSubmissions();


    List<DocumentUploadResponse> uploadMultipleDocumentsWithStaff(
            Staff staff,
            List<UUID> requirementIds,
            List<MultipartFile> files
    ) throws FileStorageException;

    Long countSubmissionsByStaffId(UUID staffId);


}