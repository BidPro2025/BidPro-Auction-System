package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "messageID")
    private Integer messageID;

    @Column(name = "senderID", length = 50)
    private String senderID;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "channelID", nullable = false)
    private Integer channelID;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderID", insertable = false, updatable = false,referencedColumnName = "admin_id")
    private Admin sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelID", insertable = false, updatable = false)
    private Channel channel;

    // Constructors
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Message(String senderID, String content, LocalDateTime timestamp, Integer channelID) {
        this.senderID = senderID;
        this.content = content;
        this.timestamp = timestamp;
        this.channelID = channelID;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getMessageID() { return messageID; }
    public void setMessageID(Integer messageID) { this.messageID = messageID; }
    public String getSenderID() { return senderID; }
    public void setSenderID(String senderID) { this.senderID = senderID; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getChannelID() { return channelID; }
    public void setChannelID(Integer channelID) { this.channelID = channelID; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
    public Admin getSender() { return sender; }
    public void setSender(Admin sender) { this.sender = sender; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
}