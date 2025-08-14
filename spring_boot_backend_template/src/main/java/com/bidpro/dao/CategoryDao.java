package com.bidpro.dao;

import com.bidpro.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category, String> {
    Category findByName(String name);
    List<Category> findByVisibilityTrue();

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.categoryID = :categoryID")
    boolean hasAssociatedProducts(@Param("categoryID") String categoryID);

    List<Category> findAll();
    List<Category> findByParentCategory(String parentCategoryID);
}
