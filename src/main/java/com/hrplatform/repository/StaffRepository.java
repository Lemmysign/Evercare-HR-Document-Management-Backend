package com.hrplatform.repository;

import com.hrplatform.dto.response.SubmissionListResponse;
import com.hrplatform.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    @Query("SELECT s FROM Staff s WHERE LOWER(s.staffIdNumber) = LOWER(:staffIdNumber)")
    Optional<Staff> findByStaffIdNumberIgnoreCase(@Param("staffIdNumber") String staffIdNumber);

    @Query("SELECT s FROM Staff s WHERE LOWER(s.email) = LOWER(:email)")
    Optional<Staff> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT s FROM Staff s WHERE LOWER(s.staffIdNumber) = LOWER(:staffIdNumber) AND LOWER(s.email) = LOWER(:email)")
    Optional<Staff> findByStaffIdNumberAndEmailIgnoreCase(@Param("staffIdNumber") String staffIdNumber,
                                                          @Param("email") String email);

    boolean existsByStaffIdNumber(String staffIdNumber);

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Staff s LEFT JOIN FETCH s.documentSubmissions WHERE s.id = :id")
    Optional<Staff> findByIdWithDocuments(@Param("id") UUID id);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") UUID departmentId);

    @Query("SELECT s FROM Staff s WHERE s.department.id = :departmentId")
    Page<Staff> findByDepartmentId(@Param("departmentId") UUID departmentId, Pageable pageable);

    @Query("SELECT s FROM Staff s WHERE " +
            "(LOWER(s.staffIdNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Staff> searchStaff(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT s FROM Staff s WHERE s.department.id = :departmentId AND " +
            "(LOWER(s.staffIdNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Staff> searchStaffByDepartment(@Param("departmentId") UUID departmentId,
                                        @Param("searchTerm") String searchTerm,
                                        Pageable pageable);

    @Query("SELECT COUNT(DISTINCT s.id) FROM Staff s " +
            "JOIN s.documentSubmissions ds")
    Long countStaffWithSubmissions();

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.id NOT IN " +
            "(SELECT DISTINCT ds.staff.id FROM DocumentSubmission ds)")
    Long countStaffWithoutSubmissions();

    // NEW: Update staff department
    @Modifying
    @Query("UPDATE Staff s SET s.department.id = :departmentId WHERE s.id = :staffId")
    void updateStaffDepartment(@Param("staffId") UUID staffId, @Param("departmentId") UUID departmentId);


    @Query("""
    SELECT s FROM Staff s
    WHERE s.id IN (
        SELECT DISTINCT ds.staff.id
        FROM DocumentSubmission ds
    )
    ORDER BY (
        SELECT MAX(ds2.createdAt)
        FROM DocumentSubmission ds2
        WHERE ds2.staff.id = s.id
    ) DESC
""")
    Page<Staff> findStaffWithRecentSubmissions(Pageable pageable);


    @Query("""
    SELECT new com.hrplatform.dto.response.SubmissionListResponse(
        s.id,
        s.staffIdNumber,
        s.fullName,
        s.email,
        COALESCE(d.name, 'Not Assigned'),
        COUNT(DISTINCT ds.id),
        CASE WHEN COUNT(DISTINCT ds.id) > 0 THEN true ELSE false END
    )
    FROM Staff s
    LEFT JOIN s.department d
    LEFT JOIN s.documentSubmissions ds
    GROUP BY s.id, s.staffIdNumber, s.fullName, s.email, d.name
    ORDER BY s.createdAt DESC
""")
    Page<SubmissionListResponse> findAllStaffWithSubmissionCount(Pageable pageable);

    @Query("""
    SELECT new com.hrplatform.dto.response.SubmissionListResponse(
        s.id,
        s.staffIdNumber,
        s.fullName,
        s.email,
        COALESCE(d.name, 'Not Assigned'),
        COUNT(DISTINCT ds.id),
        CASE WHEN COUNT(DISTINCT ds.id) > 0 THEN true ELSE false END
    )
    FROM Staff s
    LEFT JOIN s.department d
    LEFT JOIN s.documentSubmissions ds
    GROUP BY s.id, s.staffIdNumber, s.fullName, s.email, d.name
    ORDER BY COUNT(DISTINCT ds.id) DESC, s.fullName ASC
""")
    Page<SubmissionListResponse> findAllStaffOrderedBySubmissions(Pageable pageable);


    @Query("""
    SELECT new com.hrplatform.dto.response.SubmissionListResponse(
        s.id,
        s.staffIdNumber,
        s.fullName,
        s.email,
        COALESCE(d.name, 'Not Assigned'),
        COUNT(DISTINCT ds.id),
        CASE WHEN COUNT(DISTINCT ds.id) > 0 THEN true ELSE false END
    )
    FROM Staff s
    LEFT JOIN s.department d
    LEFT JOIN s.documentSubmissions ds
    WHERE 
        (:search IS NULL OR :search = '' OR 
         LOWER(s.staffIdNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
         LOWER(s.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
         LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
         LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))
    AND (:departmentId IS NULL OR d.id = :departmentId)
    GROUP BY s.id, s.staffIdNumber, s.fullName, s.email, d.name
    HAVING (:status IS NULL OR :status = '' OR 
            (:status = 'submitted' AND COUNT(DISTINCT ds.id) > 0) OR
            (:status = 'unsubmitted' AND COUNT(DISTINCT ds.id) = 0))
    ORDER BY s.createdAt DESC
""")
    Page<SubmissionListResponse> findAllStaffWithFilters(
            @Param("search") String search,
            @Param("departmentId") UUID departmentId,
            @Param("status") String status,
            Pageable pageable
    );



}