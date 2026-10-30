package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Integer logID;

    @Column(name = "entityType", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entityID", length = 50, nullable = false)
    private String entityID;

    @Column(name = "operation", length = 50, nullable = false)
    private String operation;

    @Column(name = "performedBy", length = 50)
    private String performedBy;

    @Column(name = "logTimestamp", nullable = false)
    private LocalDateTime logTimestamp;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    // Constructors
    public Log() {
        this.createdAt = LocalDateTime.now();
    }

    public Log(String entityType, String entityID, String operation, LocalDateTime logTimestamp) {
        this.entityType = entityType;
        this.entityID = entityID;
        this.operation = operation;
        this.logTimestamp = logTimestamp;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getLogID() { return logID; }
    public void setLogID(Integer logID) { this.logID = logID; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityID() { return entityID; }
    public void setEntityID(String entityID) { this.entityID = entityID; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getLogTimestamp() { return logTimestamp; }
    public void setLogTimestamp(LocalDateTime logTimestamp) { this.logTimestamp = logTimestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
}