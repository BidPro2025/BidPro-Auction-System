package com.bidpro.security;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRevokedTokenRepository extends JpaRepository<AuthRevokedToken, String> {
    @Modifying
    @Query("delete from AuthRevokedToken token where token.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") Instant now);
}
