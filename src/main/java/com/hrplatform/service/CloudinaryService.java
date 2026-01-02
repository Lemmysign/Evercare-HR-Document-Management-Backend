package com.hrplatform.service;

import com.hrplatform.dto.response.CloudinarySignatureResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface CloudinaryService {

    Map<String, Object> uploadFile(MultipartFile file, String folder) throws Exception;

    Map<String, Object> uploadFile(MultipartFile file, String folder, String customPublicId) throws Exception;

    void deleteFile(String publicId) throws Exception;

    CloudinarySignatureResponse generateUploadSignature(String staffIdNumber, UUID departmentId, String fileName);

    String generateFolder(String departmentName);
}