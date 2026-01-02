package com.hrplatform.service;

import com.hrplatform.dto.request.DocumentUploadRequest;
import com.hrplatform.dto.response.DocumentUploadResponse;
import com.hrplatform.entity.Staff;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentSubmissionService {

    DocumentUploadResponse uploadDocument(String staffIdNumber, DocumentUploadRequest request);


    Long countTotalSubmissions();


    List<DocumentUploadResponse> uploadMultipleDocumentsWithStaff(
            Staff staff,
            List<UUID> requirementIds,
            List<MultipartFile> files
    );

    Long countSubmissionsByStaffId(UUID staffId);
}