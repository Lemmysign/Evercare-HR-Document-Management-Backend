package com.hrplatform.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hrplatform.dto.response.CloudinarySignatureResponse;
import com.hrplatform.service.CloudinaryService;
import com.hrplatform.util.CloudinaryFolderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.timeout:300000}")
    private Integer timeout;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws Exception {
        log.info("Uploading file to Cloudinary: {} ({} KB)",
                file.getOriginalFilename(), file.getSize() / 1024);

        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "auto",
                        "type", "upload",
                        "access_mode", "public",
                        "timeout", timeout,
                        "chunk_size", 6000000 // 6MB chunks
                ));
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder, String customPublicId) throws Exception {
        log.info("Uploading file to Cloudinary with custom name: {} ({} KB)",
                customPublicId, file.getSize() / 1024);

        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "auto",
                        "type", "upload",
                        "access_mode", "public",
                        "public_id", customPublicId,
                        "use_filename", false,
                        "unique_filename", false,
                        "overwrite", true,
                        "timeout", timeout,
                        "chunk_size", 6000000
                ));
    }

    @Override
    public void deleteFile(String publicId) throws Exception {
        log.info("Deleting file from Cloudinary: {}", publicId);
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("timeout", timeout));
    }

    @Override
    public CloudinarySignatureResponse generateUploadSignature(
            String staffIdNumber,
            UUID departmentId,
            String fileName) {

        long timestamp = System.currentTimeMillis() / 1000L;

        String folder = CloudinaryFolderUtil.generateDepartmentFolder(departmentId.toString());
        String publicId = CloudinaryFolderUtil.generatePublicId(staffIdNumber, fileName);

        Map<String, Object> params = ObjectUtils.asMap(
                "timestamp", timestamp,
                "folder", folder,
                "public_id", publicId
        );

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        return CloudinarySignatureResponse.builder()
                .signature(signature)
                .timestamp(timestamp)
                .apiKey(apiKey)
                .cloudName(cloudName)
                .folder(folder)
                .publicId(publicId)
                .build();
    }

    @Override
    public String generateFolder(String departmentName) {
        return CloudinaryFolderUtil.generateDepartmentFolder(departmentName);
    }
}