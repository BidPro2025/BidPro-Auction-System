package com.bidpro.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidpro.dao.CategoryDao;
import com.bidpro.dto.CategoryDTO;
import com.bidpro.entities.Category;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryDao categoryDao;

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        logger.info("Creating category with name: {}", categoryDTO.getName());
        if (categoryDao.findByName(categoryDTO.getName()) != null) {
            throw new IllegalArgumentException("Category with name " + categoryDTO.getName() + " already exists");
        }

        Category category = new Category();
        category.setCategoryID(UUID.randomUUID().toString());
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setVisibility(categoryDTO.getVisibility() != null ? categoryDTO.getVisibility() : true);
        category.setDisplayOrder(categoryDTO.getDisplayOrder() != null ? categoryDTO.getDisplayOrder() : 0);
        category.setParentCategory(categoryDTO.getParentCategory());

        if (categoryDTO.getParentCategory() != null && !categoryDTO.getParentCategory().isEmpty()) {
            Category parent = categoryDao.findById(categoryDTO.getParentCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + categoryDTO.getParentCategory()));
            category.setParent(parent);
        }

        category = categoryDao.save(category);
        return toCategoryDTO(category);
    }

    public CategoryDTO getCategoryById(String categoryID) {
        logger.info("Fetching category with ID: {}", categoryID);
        Category category = categoryDao.findById(categoryID)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryID));
        return toCategoryDTO(category);
    }

    public List<CategoryDTO> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryDao.findAll().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getVisibleCategories() {
        logger.info("Fetching visible categories");
        return categoryDao.findByVisibilityTrue().stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getParentCategory(String categoryID) {
        logger.info("Fetching parent category for category ID: {}", categoryID);
        Category category = categoryDao.findById(categoryID)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryID));
        if (category.getParentCategory() == null || category.getParentCategory().isEmpty()) {
            return null;
        }
        Category parent = categoryDao.findById(category.getParentCategory())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + category.getParentCategory()));
        return toCategoryDTO(parent);
    }

    public CategoryDTO updateCategory(String categoryID, CategoryDTO categoryDTO) {
        logger.info("Updating category with ID: {}", categoryID);
        Category category = categoryDao.findById(categoryID)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryID));

        Category existingByName = categoryDao.findByName(categoryDTO.getName());
        if (existingByName != null && !existingByName.getCategoryID().equals(categoryID)) {
            throw new IllegalArgumentException("Category name " + categoryDTO.getName() + " already in use");
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setVisibility(categoryDTO.getVisibility());
        category.setDisplayOrder(categoryDTO.getDisplayOrder());
        category.setParentCategory(categoryDTO.getParentCategory());

        if (categoryDTO.getParentCategory() != null && !categoryDTO.getParentCategory().isEmpty()) {
            Category parent = categoryDao.findById(categoryDTO.getParentCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + categoryDTO.getParentCategory()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryDao.save(category);
        return toCategoryDTO(category);
    }

    public CategoryDTO updateCategoryVisibility(String categoryID, Boolean visibility) {
        logger.info("Updating visibility for category with ID: {}", categoryID);
        Category category = categoryDao.findById(categoryID)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryID));
        category.setVisibility(visibility);
        category = categoryDao.save(category);
        return toCategoryDTO(category);
    }

    public void deleteCategory(String categoryID) {
        logger.info("Deleting category with ID: {}", categoryID);
        Category category = categoryDao.findById(categoryID)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryID));
        if (categoryDao.hasAssociatedProducts(categoryID)) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }
        if (!categoryDao.findByParentCategory(categoryID).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with subcategories");
        }
        categoryDao.delete(category);
    }

    private CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryID(category.getCategoryID());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setVisibility(category.getVisibility());
        dto.setDisplayOrder(category.getDisplayOrder());
        return dto;
    }
}
