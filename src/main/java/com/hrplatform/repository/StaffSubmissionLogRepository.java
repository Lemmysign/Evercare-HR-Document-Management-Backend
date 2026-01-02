package com.hrplatform.repository;

import com.hrplatform.entity.StaffSubmissionLog;
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
public interface StaffSubmissionLogRepository extends JpaRepository<StaffSubmissionLog, UUID> {

    List<StaffSubmissionLog> findByStaffIdNumber(String staffIdNumber);

    Page<StaffSubmissionLog> findByStaffIdNumber(String staffIdNumber, Pageable pageable);

    @Query("SELECT ssl FROM StaffSubmissionLog ssl WHERE ssl.action = :action")
    List<StaffSubmissionLog> findByAction(@Param("action") String action);

    @Query("SELECT ssl FROM StaffSubmissionLog ssl " +
            "WHERE ssl.createdAt BETWEEN :startDate AND :endDate")
    List<StaffSubmissionLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}