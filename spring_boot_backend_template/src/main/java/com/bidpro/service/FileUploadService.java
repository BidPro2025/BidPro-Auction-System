package com.bidpro.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bidpro.dao.ProductImagesDao;
import com.bidpro.entities.ProductImages;

@Service
public class FileUploadService {

    @Autowired
    private ProductImagesDao productImagesDao;

    public String uploadProductImage(MultipartFile file, String productID) {
        try {
            // Simulate file upload (e.g., save to local storage or cloud)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String uploadDir = "uploads/product-images/"; // Adjust path as needed
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            // Save image metadata to database
            ProductImages image = new ProductImages();
            image.setProduct(new com.bidpro.entities.Product());
            image.getProduct().setProductID(productID);
            image.setImageUrl("/images/" + fileName); // Adjust URL path as needed
            image.setUploadedAt(LocalDateTime.now());
            productImagesDao.save(image);

            return image.getImageUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }
}