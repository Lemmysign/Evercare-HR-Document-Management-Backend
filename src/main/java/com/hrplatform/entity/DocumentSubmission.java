package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_submissions", indexes = {
        @Index(name = "idx_doc_sub_staff", columnList = "staff_id"),
        @Index(name = "idx_doc_sub_requirement", columnList = "requirement_id"),
        @Index(name = "idx_doc_sub_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private DocumentRequirement documentRequirement;

    // ✅ CORRECT: Stores relative path like "medical/lemuel_inneh_resume_12345.pdf"
    @Column(nullable = false, length = 500, name = "file_path")
    private String filePath;

    // ❌ REMOVE THIS - Not needed for local storage
    // @Column(nullable = false, length = 200, name = "public_id")
    // private String publicId;

    @Column(nullable = false, length = 200)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 150)
    private String mimeType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}