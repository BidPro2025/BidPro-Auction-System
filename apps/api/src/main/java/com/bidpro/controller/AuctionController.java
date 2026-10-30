package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/auctions")
public class AuctionController {
    private final AuctionService service;
    public AuctionController(AuctionService service) {this.service=service;}
    @GetMapping public ApiResponse<List<AuctionDTO>> all(@RequestParam(required=false) String status) {return ApiResponse.success(status==null?service.getAllAuctions():service.listByStatus(status),"Auctions retrieved");}
    @GetMapping("/{id}") public ApiResponse<AuctionDTO> one(@PathVariable String id) {return ApiResponse.success(service.findAuctionById(id),"Auction retrieved");}
    @GetMapping("/status/{status}") public ApiResponse<List<AuctionDTO>> status(@PathVariable String status) {return ApiResponse.success(service.listByStatus(status),"Auctions retrieved");}
    @PostMapping public ApiResponse<AuctionDTO> create(@Valid @RequestBody AuctionDTO dto) {return ApiResponse.success(service.createAuction(dto,null),"Auction created");}
    @PostMapping("/{id}/register") public ApiResponse<Void> register(@PathVariable String id) {service.registerForAuction(id,null);return ApiResponse.success(null,"Registered for auction");}
    @PostMapping("/{id}/close") public ApiResponse<AuctionDTO> close(@PathVariable String id) {return ApiResponse.success(service.close(id),"Auction closed");}
}
