package com.hrplatform.repository;

import com.hrplatform.entity.ExportLog;
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
public interface ExportLogRepository extends JpaRepository<ExportLog, UUID> {

    List<ExportLog> findByHrUserEmail(String hrUserEmail);

    Page<ExportLog> findByHrUserEmail(String hrUserEmail, Pageable pageable);

    @Query("SELECT el FROM ExportLog el WHERE el.createdAt BETWEEN :startDate AND :endDate")
    List<ExportLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(el) FROM ExportLog el WHERE el.hrUserEmail = :email")
    Long countByHrUserEmail(@Param("email") String email);
}