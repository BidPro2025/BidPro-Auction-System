package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.service.FileUploadService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/file-upload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping(value = "/product-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SELLER', 'BOTH')")
    public ResponseEntity<ApiResponse<String>> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productID") @NotBlank(message = "Product ID is mandatory") String productID,
            Principal principal) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("Upload attempt with empty or null file for productID: {}", productID);
                return new ResponseEntity<>(
                        ApiResponse.error("File cannot be empty", "INVALID_FILE"),
                        HttpStatus.BAD_REQUEST);
            }

            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                logger.warn("Invalid file type uploaded: {} for productID: {}", contentType, productID);
                return new ResponseEntity<>(
                        ApiResponse.error("Only image files (JPEG, PNG) are allowed", "INVALID_FILE_TYPE"),
                        HttpStatus.BAD_REQUEST);
            }

            long maxFileSize = 5 * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                logger.warn("File size exceeds limit: {} bytes for productID: {}", file.getSize(), productID);
                return new ResponseEntity<>(
                        ApiResponse.error("File size exceeds 5MB limit", "FILE_TOO_LARGE"),
                        HttpStatus.BAD_REQUEST);
            }

            logger.info("User {} is uploading image for productID: {}", principal.getName(), productID);

            String imageUrl = fileUploadService.uploadProductImage(file, productID);

            return ResponseEntity.ok(ApiResponse.success(imageUrl, "Image uploaded successfully"));
        } catch (Exception e) {
            logger.error("Failed to upload image for productID: {}. Error: {}", productID, e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to upload image: " + e.getMessage(), "UPLOAD_FAILED"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png"));
    }
}