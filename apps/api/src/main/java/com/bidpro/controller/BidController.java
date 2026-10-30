package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.BidDTO;
import com.bidpro.service.BidService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bids")
public class BidController {
    private final BidService bids;
    public BidController(BidService bids) { this.bids = bids; }

    @PostMapping
    public ResponseEntity<ApiResponse<BidDTO>> placeBid(@Valid @RequestBody BidDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(bids.placeBid(dto), "Bid placed successfully"));
    }
    @GetMapping({"", "/mine"})
    public ResponseEntity<ApiResponse<List<BidDTO>>> ownBids() {
        return ResponseEntity.ok(ApiResponse.success(bids.ownBids(), "Bids retrieved successfully"));
    }
    @GetMapping("/product/{productID}")
    public ResponseEntity<ApiResponse<List<BidDTO>>> byProduct(@PathVariable String productID) {
        return ResponseEntity.ok(ApiResponse.success(bids.findBidsByProduct(productID).stream().map(bids::toDTO).toList(), "Bids retrieved successfully"));
    }
    @GetMapping("/product/{productID}/highest")
    public ResponseEntity<ApiResponse<List<BidDTO>>> highest(@PathVariable String productID) {
        return ResponseEntity.ok(ApiResponse.success(bids.findHighestBidByProduct(productID).stream().map(bids::toDTO).toList(), "Highest bid retrieved successfully"));
    }
    @GetMapping("/bidder/{bidderID}")
    public ResponseEntity<ApiResponse<List<BidDTO>>> byBidder(@PathVariable String bidderID) {
        return ResponseEntity.ok(ApiResponse.success(bids.findBidsByBidder(bidderID).stream().map(bids::toDTO).toList(), "Bids retrieved successfully"));
    }
    @PutMapping("/{bidID}")
    public ResponseEntity<ApiResponse<BidDTO>> updateBid(@PathVariable String bidID, @Valid @RequestBody BidDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(bids.updateBid(bidID, dto), "Bid increased successfully"));
    }
}
