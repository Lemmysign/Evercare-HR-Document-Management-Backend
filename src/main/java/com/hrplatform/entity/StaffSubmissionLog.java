package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "staff_submission_logs", indexes = {
        @Index(name = "idx_staff_sub_log_staff", columnList = "staffIdNumber"),
        @Index(name = "idx_staff_sub_log_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffSubmissionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String staffIdNumber;

    @Column(nullable = false, length = 100)
    private String departmentName;

    @Column(nullable = false, length = 50)
    private String action; // VALIDATION, UPLOAD_SUCCESS, UPLOAD_FAILED

    @Column(length = 1000)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}