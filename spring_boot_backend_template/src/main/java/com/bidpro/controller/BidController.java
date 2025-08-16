package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.BidDTO;
import com.bidpro.service.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    @Autowired
    private BidService bidService;

    /* -------------------------------------------------
       PLACE BID – null-safe bidderID
       ------------------------------------------------- */
    @PostMapping
    public ResponseEntity<ApiResponse<BidDTO>> placeBid(
            @Valid @RequestBody BidDTO bidDTO,
            Principal principal) {

        // Fallback to bidderID in DTO if principal is null
        String bidderID = (principal != null) ? principal.getName() : bidDTO.getBidderID();
        BidDTO placed = bidService.placeBid(bidDTO, bidderID);
        return ResponseEntity.ok(ApiResponse.success(placed, "Bid placed successfully"));
    }

    /* -------------------------------------------------
       READ BIDS BY PRODUCT ID
       ------------------------------------------------- */
    @GetMapping("/product/{productID}")
    public ResponseEntity<ApiResponse<List<BidDTO>>> getBidsByProduct(@PathVariable String productID) {
        List<BidDTO> bids = bidService.findBidsByProduct(productID).stream()
                                      .map(BidDTO::new) // Convert Bid to BidDTO
                                      .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(bids, "Bids retrieved successfully"));
    }

    /* -------------------------------------------------
       READ BIDS BY BIDDER ID
       ------------------------------------------------- */
    @GetMapping("/bidder/{bidderID}")
    public ResponseEntity<ApiResponse<List<BidDTO>>> getBidsByBidder(@PathVariable String bidderID) {
        List<BidDTO> bids = bidService.findBidsByBidder(bidderID).stream()
                                      .map(BidDTO::new) // Convert Bid to BidDTO
                                      .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(bids, "Bids retrieved successfully"));
    }

    /* -------------------------------------------------
       GET HIGHEST BID BY PRODUCT ID
       ------------------------------------------------- */
    @GetMapping("/product/{productID}/highest")
    public ResponseEntity<ApiResponse<List<BidDTO>>> getHighestBidByProduct(@PathVariable String productID) {
        List<BidDTO> bids = bidService.findHighestBidByProduct(productID).stream()
                                      .map(BidDTO::new) // Convert Bid to BidDTO
                                      .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(bids, "Highest bid retrieved successfully"));
    }

    /* -------------------------------------------------
       UPDATE BID
       ------------------------------------------------- */
    @PutMapping("/{bidID}")
    public ResponseEntity<ApiResponse<BidDTO>> updateBid(
            @PathVariable String bidID,
            @Valid @RequestBody BidDTO bidDTO) {
        BidDTO updatedBid = bidService.updateBid(bidID, bidDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedBid, "Bid updated successfully"));
    }
}