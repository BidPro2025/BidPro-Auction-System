package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDTO extends BaseDTO {
    private Integer messageID;

    @Size(max = 50, message = "Sender ID must be at most 50 characters")
    private String senderID;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @NotBlank(message = "Timestamp is mandatory")
    private LocalDateTime timestamp;

    @NotBlank(message = "Channel ID is mandatory")
    private Integer channelID;
}