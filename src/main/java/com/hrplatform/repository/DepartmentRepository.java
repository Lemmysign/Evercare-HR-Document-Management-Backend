package com.hrplatform.repository;

import com.hrplatform.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByName(String name);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) = LOWER(:name)")
    Optional<Department> findByNameIgnoreCase(@Param("name") String name);

    boolean existsByName(String name);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.documentRequirements WHERE d.id = :id")
    Optional<Department> findByIdWithRequirements(@Param("id") UUID id);

    @Query("SELECT COUNT(DISTINCT ds.staff.id) FROM DocumentSubmission ds " +
            "WHERE ds.staff.department.id = :departmentId")
    Long countStaffWithSubmissionsByDepartment(@Param("departmentId") UUID departmentId);

    @Query("SELECT COUNT(ds) FROM DocumentSubmission ds " +
            "WHERE ds.staff.department.id = :departmentId")
    Long countDocumentSubmissionsByDepartment(@Param("departmentId") UUID departmentId);
}