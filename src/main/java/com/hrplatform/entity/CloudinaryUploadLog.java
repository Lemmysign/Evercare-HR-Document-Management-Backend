package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cloudinary_upload_logs", indexes = {
        @Index(name = "idx_cloud_log_staff", columnList = "staffIdNumber"),
        @Index(name = "idx_cloud_log_created", columnList = "createdAt"),
        @Index(name = "idx_cloud_log_status", columnList = "uploadStatus")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudinaryUploadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String staffIdNumber;

    @Column(nullable = false, length = 200)
    private String documentName;

    @Column(nullable = false, length = 100)
    private String departmentName;

    @Column(nullable = false, length = 20)
    private String uploadStatus; // SUCCESS, FAILED

    @Column(length = 500)
    private String cloudinaryUrl;

    @Column(length = 200)
    private String cloudinaryPublicId;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Long fileSize;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}