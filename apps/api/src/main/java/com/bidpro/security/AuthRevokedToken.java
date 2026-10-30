package com.bidpro.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "auth_revoked_token", indexes = @Index(name = "ix_auth_revocation_expiry", columnList = "expires_at"))
public class AuthRevokedToken {
    @Id @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected AuthRevokedToken() {}
    public AuthRevokedToken(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
}
