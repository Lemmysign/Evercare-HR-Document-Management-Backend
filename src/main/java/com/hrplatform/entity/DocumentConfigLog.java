package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_config_logs", indexes = {
        @Index(name = "idx_doc_config_hr_user", columnList = "hrUserEmail"),
        @Index(name = "idx_doc_config_department", columnList = "departmentName"),
        @Index(name = "idx_doc_config_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentConfigLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String hrUserEmail;

    @Column(nullable = false, length = 100)
    private String departmentName;

    @Column(nullable = false, length = 50)
    private String action; // ADD, EDIT, DELETE, MARK_REQUIRED

    @Column(nullable = false, length = 200)
    private String documentName;

    @Column(length = 1000)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}