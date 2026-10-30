package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_threads", indexes = @Index(name = "idx_support_owner_updated", columnList = "owner_id,updated_at"))
@Getter @Setter
public class SupportThread {
    @Id @Column(name="thread_id", length=50) private String threadID;
    @Column(name="owner_id", length=50, nullable=false) private String ownerID;
    @Column(name="created_by", length=50, nullable=false) private String createdBy;
    @Column(nullable=false, length=120) private String subject;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10) private Status status=Status.OPEN;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
    @Column(name="owner_read_at") private LocalDateTime ownerReadAt;
    @Column(name="admin_read_at") private LocalDateTime adminReadAt;
    public enum Status { OPEN, CLOSED }
}
