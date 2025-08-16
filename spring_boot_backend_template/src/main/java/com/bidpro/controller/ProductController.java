package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.ProductDTO;
import com.bidpro.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /* -------------------------------------------------
       CREATE PRODUCT – null-safe sellerID
       ------------------------------------------------- */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            Principal principal) {

        // Fallback to sellerID in DTO if principal is null
        String sellerID = (principal != null) ? principal.getName() : productDTO.getSellerID();
        ProductDTO created = productService.createProduct(productDTO, sellerID);
        return ResponseEntity.ok(ApiResponse.success(created, "Product created successfully"));
    }

    /* -------------------------------------------------
       READ
       ------------------------------------------------- */
    @GetMapping("/{productID}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable String productID) {
        ProductDTO product = productService.findProductById(productID);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    @GetMapping("/category/{categoryID}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String categoryID) {
        List<ProductDTO> products = productService.findProductsByCategory(categoryID);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    /* -------------------------------------------------
       UPDATE – null-safe sellerID
       ------------------------------------------------- */
    @PutMapping("/{productID}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable String productID,
            @Valid @RequestBody ProductDTO productDTO,
            Principal principal) {

        String sellerID = (principal != null) ? principal.getName() : productDTO.getSellerID();
        ProductDTO updated = productService.updateProduct(productID, productDTO, sellerID);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    /* -------------------------------------------------
       DELETE
       ------------------------------------------------- */
    @DeleteMapping("/{productID}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productID) {
        productService.deleteProduct(productID);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}