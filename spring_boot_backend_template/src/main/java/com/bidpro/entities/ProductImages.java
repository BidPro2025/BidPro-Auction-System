package com.bidpro.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageID")
    private Integer imageID;

    @Column(name = "productID", length = 50, nullable = false)
    private String productID;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "uploadedAt", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID", insertable = false, updatable = false)
    private Product product;

    // Constructors
    public ProductImages() {
        this.uploadedAt = LocalDateTime.now();
    }

    public ProductImages(String productID, String imageUrl) {
        this.productID = productID;
        this.imageUrl = imageUrl;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getImageID() { return imageID; }
    public void setImageID(Integer imageID) { this.imageID = imageID; }
    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getExtension1() { return extension1; }
    public void setExtension1(String extension1) { this.extension1 = extension1; }
    public String getExtension2() { return extension2; }
    public void setExtension2(String extension2) { this.extension2 = extension2; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}