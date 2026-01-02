package com.hrplatform.repository;

import com.hrplatform.entity.HrUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HrUserRepository extends JpaRepository<HrUser, UUID> {

    @Query("SELECT h FROM HrUser h WHERE LOWER(h.email) = LOWER(:email)")
    Optional<HrUser> findByEmailIgnoreCase(@Param("email") String email);

    Optional<HrUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(h) FROM HrUser h WHERE h.isActive = true")
    Long countActiveUsers();

    Optional<HrUser> findByPasswordResetToken(String token);

    @Modifying
    @Query("UPDATE HrUser h SET h.passwordResetToken = null, h.passwordResetTokenExpiry = null WHERE h.email = :email")
    void clearPasswordResetToken(@Param("email") String email);
}