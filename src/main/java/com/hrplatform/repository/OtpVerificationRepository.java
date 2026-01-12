package com.hrplatform.repository;

import com.hrplatform.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    @Query("SELECT o FROM OtpVerification o WHERE o.staff.id = :staffId " +
            "AND o.isUsed = false AND o.expiresAt > :now " +
            "ORDER BY o.createdAt DESC")
    Optional<OtpVerification> findLatestValidOtpByStaffId(
            @Param("staffId") UUID staffId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT o FROM OtpVerification o WHERE o.staff.id = :staffId " +
            "AND o.otpCode = :otpCode AND o.isUsed = false " +
            "AND o.expiresAt > :now")
    Optional<OtpVerification> findValidOtp(
            @Param("staffId") UUID staffId,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE OtpVerification o SET o.isUsed = true WHERE o.staff.id = :staffId")
    void invalidateAllOtpForStaff(@Param("staffId") UUID staffId);

    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.staff.id = :staffId " +
            "AND o.createdAt > :since")
    Long countOtpSentSince(@Param("staffId") UUID staffId, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now OR o.createdAt < :cutoff")
    void deleteExpiredOtps(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    List<OtpVerification> findByStaffIdOrderByCreatedAtDesc(UUID staffId);
}