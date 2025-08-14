package com.bidpro.dto;

import com.bidpro.entities.AuctionRegistration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuctionRegistrationDTO extends BaseDTO {
    @NotBlank(message = "Auction ID is mandatory")
    @Size(max = 50, message = "Auction ID must be at most 50 characters")
    private String auctionID;

    @NotBlank(message = "User ID is mandatory")
    @Size(max = 50, message = "User ID must be at most 50 characters")
    private String userID;

    @NotBlank(message = "Registration status is mandatory")
    private AuctionRegistration.RegistrationStatus registrationStatus;

    private LocalDateTime registeredAt;
}