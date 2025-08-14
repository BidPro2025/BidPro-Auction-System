package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductImagesDTO extends BaseDTO {
    private Integer imageID;

    @NotBlank(message = "Product ID is mandatory")
    @Size(max = 50, message = "Product ID must be at most 50 characters")
    private String productID;

    @NotBlank(message = "Image URL is mandatory")
    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String imageUrl;

    @NotBlank(message = "Uploaded at is mandatory")
    private LocalDateTime uploadedAt;
}