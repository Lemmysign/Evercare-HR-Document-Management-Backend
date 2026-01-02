package com.hrplatform.repository;

import com.hrplatform.entity.DocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, UUID> {

    @Query("SELECT dr FROM DocumentRequirement dr WHERE dr.department.id = :departmentId AND dr.isActive = true")
    List<DocumentRequirement> findByDepartmentIdAndIsActiveTrue(@Param("departmentId") UUID departmentId);

    @Query("SELECT dr FROM DocumentRequirement dr WHERE dr.department.id = :departmentId")
    List<DocumentRequirement> findByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT dr FROM DocumentRequirement dr WHERE dr.department.id = :departmentId " +
            "AND LOWER(dr.documentName) = LOWER(:documentName)")
    Optional<DocumentRequirement> findByDepartmentIdAndDocumentName(@Param("departmentId") UUID departmentId,
                                                                    @Param("documentName") String documentName);

    @Query("SELECT COUNT(dr) FROM DocumentRequirement dr WHERE dr.department.id = :departmentId AND dr.isRequired = true")
    Long countRequiredDocumentsByDepartment(@Param("departmentId") UUID departmentId);

    boolean existsByDepartmentIdAndDocumentNameIgnoreCase(UUID departmentId, String documentName);
}