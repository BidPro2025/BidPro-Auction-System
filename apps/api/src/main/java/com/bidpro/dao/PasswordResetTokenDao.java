package com.bidpro.dao;

import com.bidpro.entities.PasswordResetToken;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenDao extends JpaRepository<PasswordResetToken, String> {
    @Query("select r.userID from PasswordResetToken r where r.tokenHash = :hash")
    Optional<String> findUserID(@Param("hash") String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PasswordResetToken r where r.tokenHash = :hash")
    Optional<PasswordResetToken> findForUpdate(@Param("hash") String hash);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PasswordResetToken r set r.usedAt = :now where r.userID = :userID and r.usedAt is null")
    void invalidateForUser(@Param("userID") String userID, @Param("now") LocalDateTime now);
}
