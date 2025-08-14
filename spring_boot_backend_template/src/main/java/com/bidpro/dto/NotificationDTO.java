package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO extends BaseDTO {
    private Integer notificationID;

    @NotBlank(message = "User ID is mandatory")
    @Size(max = 50, message = "User ID must be at most 50 characters")
    private String userID;

    @NotBlank(message = "Type is mandatory")
    @Size(max = 100, message = "Type must be at most 100 characters")
    private String type;

    @NotBlank(message = "Content is mandatory")
    @Size(max = 255, message = "Content must be at most 255 characters")
    private String content;

    @NotBlank(message = "Timestamp is mandatory")
    private LocalDateTime timestamp;

    private Boolean isRead;
}