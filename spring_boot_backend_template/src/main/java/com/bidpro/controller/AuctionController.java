package com.bidpro.controller;

import com.bidpro.dto.AuctionDTO;
import com.bidpro.dto.ApiResponse;
import com.bidpro.entities.Auction;
import com.bidpro.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /* -------------------------------------------------
       CREATE AUCTION – admin only
       ------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuctionDTO>> createAuction(
            @Valid @RequestBody AuctionDTO auctionDTO,
            Principal principal) {

        String adminID = (principal != null) ? principal.getName() : auctionDTO.getCreatedBy();
        AuctionDTO created = auctionService.createAuction(auctionDTO, adminID);
        return ResponseEntity.ok(ApiResponse.success(created, "Auction created successfully"));
    }

    /* -------------------------------------------------
       GET AUCTION BY ID
       ------------------------------------------------- */
    @GetMapping("/{auctionID}")
    public ResponseEntity<ApiResponse<AuctionDTO>> getAuctionById(@PathVariable String auctionID) {
        AuctionDTO auction = auctionService.findAuctionById(auctionID);
        return ResponseEntity.ok(ApiResponse.success(auction, "Auction retrieved successfully"));
    }

    /* -------------------------------------------------
       REGISTER FOR AUCTION – authenticated user
       ------------------------------------------------- */
    @PostMapping("/{auctionID}/register")
    public ResponseEntity<ApiResponse<String>> registerForAuction(
            @PathVariable String auctionID,
            Principal principal) {

        if (principal == null || principal.getName() == null) {
            // ⇒ never touch principal.getName() when it’s null
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Authentication required", "AUTH_REQUIRED"));
        }

        String userID = principal.getName();
        auctionService.registerForAuction(auctionID, userID);
        return ResponseEntity.ok(ApiResponse.success(null, "Registered for auction successfully"));
    }

    /* -------------------------------------------------
       GET AUCTIONS BY STATUS
       ------------------------------------------------- */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AuctionDTO>>> getAuctionsByStatus(@PathVariable String status) {
        List<AuctionDTO> auctions = auctionService.findAuctionsByStatus(Auction.Status.valueOf(status)).stream()
                .map(AuctionDTO::new) // Use the constructor to convert Auction to AuctionDTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(auctions, "Auctions retrieved successfully"));
    }
}

