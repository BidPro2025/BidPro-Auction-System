package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/response")
public class ApiResponseController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testSuccessResponse() {
        return ResponseEntity.ok(ApiResponse.success("Test data", "Success response test completed"));
    }

    @GetMapping("/error")
    public ResponseEntity<ApiResponse<String>> testErrorResponse() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("An error occurred", "ERR_001"));
    }

    @GetMapping("/custom-status")
    public ResponseEntity<ApiResponse<String>> testCustomStatusResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found", "ERR_404", HttpStatus.NOT_FOUND.value()));
    }

    @GetMapping("/typed-data")
    public ResponseEntity<ApiResponse<Integer>> testTypedDataResponse() {
        return ResponseEntity.ok(ApiResponse.success(42, "Integer data response"));
    }
}