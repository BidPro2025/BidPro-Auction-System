package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activityID")
    private Integer activityID;

    @Column(name = "userID", length = 50, nullable = false)
    private String userID;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "details", length = 255)
    private String details;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", insertable = false, updatable = false)
    private User user;

    // Constructors
    public Activity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Activity(String userID, String action, LocalDateTime timestamp) {
        this.userID = userID;
        this.action = action;
        this.timestamp = timestamp;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getActivityID() { return activityID; }
    public void setActivityID(Integer activityID) { this.activityID = activityID; }
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}