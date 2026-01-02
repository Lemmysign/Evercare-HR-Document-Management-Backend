package com.hrplatform.repository;

import com.hrplatform.entity.CloudinaryUploadLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CloudinaryUploadLogRepository extends JpaRepository<CloudinaryUploadLog, UUID> {

    List<CloudinaryUploadLog> findByStaffIdNumber(String staffIdNumber);

    @Query("SELECT cul FROM CloudinaryUploadLog cul WHERE cul.uploadStatus = :status")
    Page<CloudinaryUploadLog> findByUploadStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT cul FROM CloudinaryUploadLog cul " +
            "WHERE cul.createdAt BETWEEN :startDate AND :endDate")
    List<CloudinaryUploadLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(cul) FROM CloudinaryUploadLog cul WHERE cul.uploadStatus = 'SUCCESS'")
    Long countSuccessfulUploads();

    @Query("SELECT COUNT(cul) FROM CloudinaryUploadLog cul WHERE cul.uploadStatus = 'FAILED'")
    Long countFailedUploads();
}