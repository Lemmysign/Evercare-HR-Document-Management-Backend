package com.hrplatform.mapper;

import com.hrplatform.entity.CloudinaryUploadLog;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryUploadLogMapper {

    public CloudinaryUploadLog toEntity(
            String staffIdNumber,
            String documentName,
            String departmentName,
            String uploadStatus,
            String cloudinaryUrl,
            String cloudinaryPublicId,
            String errorMessage,
            Long fileSize) {

        return CloudinaryUploadLog.builder()
                .staffIdNumber(staffIdNumber)
                .documentName(documentName)
                .departmentName(departmentName)
                .uploadStatus(uploadStatus)
                .cloudinaryUrl(cloudinaryUrl)
                .cloudinaryPublicId(cloudinaryPublicId)
                .errorMessage(errorMessage)
                .fileSize(fileSize)
                .build();
    }
}