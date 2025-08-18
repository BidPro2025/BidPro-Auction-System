package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelDTO extends BaseDTO {
    @Size(max = 50, message = "Channel ID must be at most 50 characters")
    private Integer channelID;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotBlank(message = "Type is mandatory")
    @Size(max = 50, message = "Type must be at most 50 characters")
    private String type;
}