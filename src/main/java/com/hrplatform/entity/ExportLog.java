package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_logs", indexes = {
        @Index(name = "idx_export_hr_user", columnList = "hrUserEmail"),
        @Index(name = "idx_export_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String hrUserEmail;

    @Column(length = 100)
    private String departmentFilter;

    @Column(length = 50)
    private String submissionStatusFilter;

    @Column
    private LocalDateTime dateRangeStart;

    @Column
    private LocalDateTime dateRangeEnd;

    @Column(nullable = false)
    private Integer totalRecordsExported;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}