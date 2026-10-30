package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "password_reset_token", indexes = @Index(name = "idx_reset_user", columnList = "user_id"))
@Getter @Setter
public class PasswordResetToken {
    @Id @Column(length = 64) private String tokenHash;
    @Column(name = "user_id", nullable = false, length = 50) private String userID;
    @Column(nullable = false) private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}
