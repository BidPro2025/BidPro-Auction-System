package com.bidpro.dao;

import com.bidpro.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDao extends JpaRepository<Product, String> {
    Optional<Product> findByTitle(String title);
    List<Product> findBySellerUserID(String sellerID);
    List<Product> findByCategoryCategoryID(String categoryID);
    List<Product> findByAuctionAuctionID(String auctionID);
    List<Product> findByProductStatus(Product.ProductStatus status);

    @Query("select p from Product p where p.title like %:keyword% or p.description like %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("select p from Product p where p.startingPrice between :minPrice and :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);
}