package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ActivityDTO extends BaseDTO {
    private Integer activityID;

    @NotBlank(message = "User ID is mandatory")
    @Size(max = 50, message = "User ID must be at most 50 characters")
    private String userID;

    @NotBlank(message = "Action is mandatory")
    @Size(max = 50, message = "Action must be at most 50 characters")
    private String action;

    @NotBlank(message = "Timestamp is mandatory")
    private LocalDateTime timestamp;

    @Size(max = 255, message = "Details must be at most 255 characters")
    private String details;

    private String metadata;
}