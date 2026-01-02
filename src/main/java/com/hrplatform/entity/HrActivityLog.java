package com.hrplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hr_activity_logs", indexes = {
        @Index(name = "idx_hr_activity_email", columnList = "hrUserEmail"),
        @Index(name = "idx_hr_activity_created", columnList = "createdAt"),
        @Index(name = "idx_hr_activity_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String hrUserEmail;

    @Column(nullable = false, length = 100)
    private String action; // EXPORT, LOGIN, PASSWORD_RESET, CONFIG_CHANGE

    @Column(length = 100)
    private String targetDepartment;

    @Column(length = 2000)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}