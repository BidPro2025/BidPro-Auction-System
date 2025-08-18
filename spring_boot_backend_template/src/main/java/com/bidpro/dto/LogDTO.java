package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogDTO extends BaseDTO {
    private Integer logID;

    @NotBlank(message = "Entity type is mandatory")
    @Size(max = 50, message = "Entity type must be at most 50 characters")
    private String entityType;

    @NotBlank(message = "Entity ID is mandatory")
    @Size(max = 50, message = "Entity ID must be at most 50 characters")
    private String entityID;

    @NotBlank(message = "Operation is mandatory")
    @Size(max = 50, message = "Operation must be at most 50 characters")
    private String operation;

    @Size(max = 50, message = "Performed by must be at most 50 characters")
    private String performedBy;

    @NotBlank(message = "Log timestamp is mandatory")
    private LocalDateTime logTimestamp;

    private String details;

    private String metadata;
}