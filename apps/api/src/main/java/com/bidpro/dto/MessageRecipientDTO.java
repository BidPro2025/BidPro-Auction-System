package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRecipientDTO extends BaseDTO {
    @NotBlank(message = "Message ID is mandatory")
    private Integer messageID;

    @NotBlank(message = "User ID is mandatory")
    @Size(max = 50, message = "User ID must be at most 50 characters")
    private String userID;
}