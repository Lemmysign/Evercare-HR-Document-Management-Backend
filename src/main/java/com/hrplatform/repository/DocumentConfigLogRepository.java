package com.hrplatform.repository;

import com.hrplatform.entity.DocumentConfigLog;
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
public interface DocumentConfigLogRepository extends JpaRepository<DocumentConfigLog, UUID> {

    List<DocumentConfigLog> findByDepartmentName(String departmentName);

    Page<DocumentConfigLog> findByDepartmentName(String departmentName, Pageable pageable);

    List<DocumentConfigLog> findByHrUserEmail(String hrUserEmail);

    @Query("SELECT dcl FROM DocumentConfigLog dcl " +
            "WHERE dcl.createdAt BETWEEN :startDate AND :endDate")
    List<DocumentConfigLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT dcl FROM DocumentConfigLog dcl " +
            "WHERE dcl.departmentName = :departmentName " +
            "AND dcl.createdAt BETWEEN :startDate AND :endDate")
    List<DocumentConfigLog> findByDepartmentNameAndDateRange(@Param("departmentName") String departmentName,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);
}