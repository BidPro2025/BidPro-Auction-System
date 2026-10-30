package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "email_outbox")
@Getter @Setter
public class EmailOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String recipient;
    @Column(nullable = false, length = 200) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;
    private LocalDateTime nextAttemptAt;
    @Column(nullable = false) private int attempts;
    @Column(length = 255) private String lastError;
    @Column(name = "is_sensitive", nullable = false) private boolean sensitive;
    public enum Status { LOCAL, QUEUED, SENT, FAILED }
}
