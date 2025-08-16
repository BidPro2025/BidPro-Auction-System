package com.bidpro.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.CategoryDTO;
import com.bidpro.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(ApiResponse.success(createdCategory, "Category created successfully"));
    }

    @GetMapping("/{categoryID}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable String categoryID) {
        CategoryDTO category = categoryService.getCategoryById(categoryID);
        return ResponseEntity.ok(ApiResponse.success(category, "Category retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getVisibleCategories() {
        List<CategoryDTO> categories = categoryService.getVisibleCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    @DeleteMapping("/{categoryID}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable String categoryID) {
        categoryService.deleteCategory(categoryID);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
