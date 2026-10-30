package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name="support_messages", indexes=@Index(name="idx_support_messages_thread", columnList="thread_id,created_at"))
@Getter @Setter
public class SupportMessage {
    @Id @Column(name="message_id", length=50) private String messageID;
    @Column(name="thread_id", length=50, nullable=false) private String threadID;
    @Column(name="sender_id", length=50, nullable=false) private String senderID;
    @Column(nullable=false, length=4000) private String content;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
