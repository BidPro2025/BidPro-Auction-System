package com.bidpro.dao;

import com.bidpro.entities.Product;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCatalogRepository {
    Page<Product> searchPublicCatalog(String query, String category, String status,
            String sort, LocalDateTime now, Pageable pageable);
}
