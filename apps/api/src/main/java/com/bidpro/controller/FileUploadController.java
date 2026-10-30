package com.bidpro.controller;
import com.bidpro.dto.ApiResponse;
import com.bidpro.service.FileUploadService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class FileUploadController {
    private final FileUploadService service;
    public FileUploadController(FileUploadService service) {this.service=service;}
    @PostMapping(value="/api/file-upload/product-image",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file,@RequestParam String productID) {
        return ApiResponse.success(service.uploadProductImage(file,productID),"Image uploaded");
    }
    @GetMapping("/api/images/{name}") public ResponseEntity<Resource> image(@PathVariable String name) {
        return ResponseEntity.ok().contentType(name.endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG)
            .header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)))
            .body(service.load(name));
    }
}
