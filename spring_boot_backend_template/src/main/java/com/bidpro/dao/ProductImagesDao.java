package com.bidpro.dao;

import com.bidpro.entities.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImagesDao extends JpaRepository<ProductImages, Integer> {
    List<ProductImages> findByProductProductID(String productID);
}