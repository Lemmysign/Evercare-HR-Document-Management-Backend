package com.hrplatform.repository;

import com.hrplatform.entity.HrActivityLog;
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
public interface HrActivityLogRepository extends JpaRepository<HrActivityLog, UUID> {

    List<HrActivityLog> findByHrUserEmail(String hrUserEmail);

    Page<HrActivityLog> findByHrUserEmail(String hrUserEmail, Pageable pageable);

    @Query("SELECT hal FROM HrActivityLog hal WHERE hal.action = :action")
    Page<HrActivityLog> findByAction(@Param("action") String action, Pageable pageable);

    @Query("SELECT hal FROM HrActivityLog hal " +
            "WHERE hal.createdAt BETWEEN :startDate AND :endDate")
    List<HrActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT hal FROM HrActivityLog hal WHERE hal.hrUserEmail = :email " +
            "AND hal.createdAt BETWEEN :startDate AND :endDate")
    List<HrActivityLog> findByHrUserEmailAndDateRange(@Param("email") String email,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
}