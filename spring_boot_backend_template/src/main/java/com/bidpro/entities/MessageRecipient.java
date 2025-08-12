package com.bidpro.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_recipient")
@IdClass(MessageRecipient.MessageRecipientId.class)
public class MessageRecipient {
    @Id
    @Column(name = "messageID")
    private Integer messageID;

    @Id
    @Column(name = "userID", length = 50)
    private String userID;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "messageID", insertable = false, updatable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", insertable = false, updatable = false)
    private User user;

    // Composite key field
    private MessageRecipientId id;

    // Constructors
    public MessageRecipient() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public MessageRecipient(Integer messageID, String userID) {
        this.messageID = messageID;
        this.userID = userID;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.id = new MessageRecipientId(messageID, userID);
    }

    // Getters and Setters
    public MessageRecipientId getId() {
        return id;
    }

    public void setId(MessageRecipientId id) {
        this.id = id;
        this.messageID = id.getMessageID();
        this.userID = id.getUserID();
    }

    public Integer getMessageID() { return messageID; }
    public void setMessageID(Integer messageID) { this.messageID = messageID; }
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Nested IdClass
    public static class MessageRecipientId implements Serializable {
        private Integer messageID;
        private String userID;

        public MessageRecipientId() {}

        public MessageRecipientId(Integer messageID, String userID) {
            this.messageID = messageID;
            this.userID = userID;
        }

        public Integer getMessageID() {
            return messageID;
        }

        public void setMessageID(Integer messageID) {
            this.messageID = messageID;
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageRecipientId that = (MessageRecipientId) o;
            return messageID.equals(that.messageID) && userID.equals(that.userID);
        }

        @Override
        public int hashCode() {
            return 31 * messageID.hashCode() + userID.hashCode();
        }
    }
}