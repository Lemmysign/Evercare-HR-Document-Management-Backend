package com.hrplatform.repository;

import com.hrplatform.entity.DocumentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentSubmissionRepository extends JpaRepository<DocumentSubmission, UUID> {

    @Query("SELECT ds FROM DocumentSubmission ds WHERE ds.staff.id = :staffId")
    List<DocumentSubmission> findByStaffId(@Param("staffId") UUID staffId);

    @Query("SELECT COUNT(ds) FROM DocumentSubmission ds WHERE ds.staff.id = :staffId")
    Long countByStaffId(@Param("staffId") UUID staffId);

    @Query("SELECT ds FROM DocumentSubmission ds WHERE ds.staff.department.id = :departmentId")
    List<DocumentSubmission> findByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT COUNT(ds) FROM DocumentSubmission ds")
    Long countAllSubmissions();

    @Query("SELECT ds FROM DocumentSubmission ds " +
            "WHERE ds.createdAt BETWEEN :startDate AND :endDate")
    List<DocumentSubmission> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ds FROM DocumentSubmission ds " +
            "WHERE ds.staff.department.id = :departmentId " +
            "AND ds.createdAt BETWEEN :startDate AND :endDate")
    List<DocumentSubmission> findByDepartmentIdAndDateRange(@Param("departmentId") UUID departmentId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ds FROM DocumentSubmission ds " +
            "JOIN FETCH ds.staff s " +
            "JOIN FETCH ds.documentRequirement dr " +
            "WHERE s.id = :staffId")
    List<DocumentSubmission> findByStaffIdWithDetails(@Param("staffId") UUID staffId);

    boolean existsByStaffIdAndDocumentRequirementId(UUID staffId, UUID requirementId);

    // New method to support re-upload functionality
    Optional<DocumentSubmission> findByStaffIdAndDocumentRequirementId(UUID staffId, UUID requirementId);

    Optional<DocumentSubmission> findTopByStaffIdOrderByCreatedAtDesc(UUID staffId);
}