package com.bidpro.service;

import com.bidpro.dao.CategoryDao;
import com.bidpro.dao.ProductDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.ProductDTO;
import com.bidpro.entities.Category;
import com.bidpro.entities.Product;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String DEFAULT_ADMIN_ID = "0692ba14-da4c-42f5-b255-431c41f7c4aa";

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private UserDao userDao;

    public ProductDTO createProduct(ProductDTO productDTO, String adminID) {
        logger.info("Creating product with provided adminID: {}", adminID);
        // Use default admin ID if provided adminID is null or empty
        String effectiveAdminID = (adminID != null && !adminID.isEmpty()) ? adminID : DEFAULT_ADMIN_ID;
        logger.info("Using effective adminID: {}", effectiveAdminID);

        User admin = userDao.findById(effectiveAdminID)
                .orElseThrow(() -> {
                    logger.error("Admin not found in database with ID: {}", effectiveAdminID);
                    return new EntityNotFoundException("Admin not found with ID: " + effectiveAdminID);
                });

        logger.info("Found user with ID: {}, Role: {}", admin.getUserID(), admin.getRole());
        if (!admin.getRole().equals(User.Role.ADMIN)) {
            logger.error("User with ID: {} is not an ADMIN, role is: {}", effectiveAdminID, admin.getRole());
            throw new IllegalArgumentException("Only admins can create products");
        }

        if (productDao.findByTitle(productDTO.getTitle()).isPresent()) {
            logger.warn("Product with title {} already exists", productDTO.getTitle());
            throw new IllegalArgumentException("Product with title " + productDTO.getTitle() + " already exists");
        }

        Category category = categoryDao.findById(productDTO.getCategoryID())
                .orElseThrow(() -> {
                    logger.error("Category not found: {}", productDTO.getCategoryID());
                    return new EntityNotFoundException("Category not found: " + productDTO.getCategoryID());
                });

        Product product = new Product();
        product.setProductID(UUID.randomUUID().toString());
        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setSellerID(effectiveAdminID); // Use effective admin ID
        product.setCategoryID(productDTO.getCategoryID());
        product.setCategory(category);
        product.setProductStatus(productDTO.getProductStatus());
        product.setAuctionStartDate(productDTO.getAuctionStartDate());
        product.setAuctionEndDate(productDTO.getAuctionEndDate());
        product.setStartingPrice(productDTO.getStartingPrice());
        product.setIncrementGap(productDTO.getIncrementGap());
        product.setProductCondition(productDTO.getProductCondition());
        product.setPromotionStatus(productDTO.getPromotionStatus());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        product = productDao.save(product);
        logger.info("Product created with ID: {}", product.getProductID());
        productDTO.setProductID(product.getProductID());
        productDTO.setSellerID(effectiveAdminID);
        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());
        return productDTO;
    }

    public ProductDTO findProductById(String productID) {
        logger.info("Fetching product with ID: {}", productID);
        Product product = productDao.findById(productID)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productID));
        return toProductDTO(product);
    }

    public List<ProductDTO> findProductsByCategory(String categoryID) {
        logger.info("Fetching products for categoryID: {}", categoryID);
        return productDao.findByCategoryCategoryID(categoryID).stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String keyword) {
        logger.info("Searching products with keyword: {}", keyword);
        return productDao.searchProducts(keyword).stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO updateProduct(String productID, ProductDTO productDTO, String adminID) {
        logger.info("Updating product with ID: {} by adminID: {}", productID, adminID);
        // Use default admin ID if provided adminID is null or empty
        String effectiveAdminID = (adminID != null && !adminID.isEmpty()) ? adminID : DEFAULT_ADMIN_ID;
        logger.info("Using effective adminID: {}", effectiveAdminID);

        Product product = productDao.findById(productID)
                .orElseThrow(() -> {
                    logger.error("Product not found: {}", productID);
                    return new EntityNotFoundException("Product not found: " + productID);
                });

        User admin = userDao.findById(effectiveAdminID)
                .orElseThrow(() -> {
                    logger.error("Admin not found in database with ID: {}", effectiveAdminID);
                    return new EntityNotFoundException("Admin not found with ID: " + effectiveAdminID);
                });

        logger.info("Found user with ID: {}, Role: {}", admin.getUserID(), admin.getRole());
        if (!admin.getRole().equals(User.Role.ADMIN)) {
            logger.error("User with ID: {} is not an ADMIN, role is: {}", effectiveAdminID, admin.getRole());
            throw new IllegalArgumentException("Only admins can update products");
        }

        if (!product.getProductStatus().equals(Product.ProductStatus.PENDING)) {
            logger.warn("Cannot update product with ID: {}, status is: {}", productID, product.getProductStatus());
            throw new IllegalArgumentException("Can only update pending products");
        }

        Product existingByTitle = productDao.findByTitle(productDTO.getTitle())
                .orElse(null);
        if (existingByTitle != null && !existingByTitle.getProductID().equals(productID)) {
            logger.warn("Product title {} already in use by another product", productDTO.getTitle());
            throw new IllegalArgumentException("Product title " + productDTO.getTitle() + " already in use");
        }

        Category category = categoryDao.findById(productDTO.getCategoryID())
                .orElseThrow(() -> {
                    logger.error("Category not found: {}", productDTO.getCategoryID());
                    return new EntityNotFoundException("Category not found: " + productDTO.getCategoryID());
                });

        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setCategoryID(productDTO.getCategoryID());
        product.setCategory(category);
        product.setProductStatus(productDTO.getProductStatus());
        product.setAuctionStartDate(productDTO.getAuctionStartDate());
        product.setAuctionEndDate(productDTO.getAuctionEndDate());
        product.setStartingPrice(productDTO.getStartingPrice());
        product.setIncrementGap(productDTO.getIncrementGap());
        product.setProductCondition(productDTO.getProductCondition());
        product.setPromotionStatus(productDTO.getPromotionStatus());
        product.setUpdatedAt(LocalDateTime.now());

        product = productDao.save(product);
        logger.info("Product updated with ID: {}", product.getProductID());
        productDTO.setProductID(product.getProductID());
        productDTO.setSellerID(product.getSellerID());
        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());
        return productDTO;
    }

    public void deleteProduct(String productID) {
        logger.info("Deleting product with ID: {}", productID);
        Product product = productDao.findById(productID)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productID));
        if (!product.getProductStatus().equals(Product.ProductStatus.PENDING)) {
            throw new IllegalArgumentException("Can only delete pending products");
        }
        productDao.delete(product);
        logger.info("Product deleted with ID: {}", productID);
    }

    private ProductDTO toProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductID(product.getProductID());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setSellerID(product.getSellerID());
        dto.setCategoryID(product.getCategoryID());
        dto.setAuctionID(product.getAuctionID());
        dto.setProductStatus(product.getProductStatus());
        dto.setAuctionStartDate(product.getAuctionStartDate());
        dto.setAuctionEndDate(product.getAuctionEndDate());
        dto.setStartingPrice(product.getStartingPrice());
        dto.setIncrementGap(product.getIncrementGap());
        dto.setProductCondition(product.getProductCondition());
        dto.setPromotionStatus(product.getPromotionStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    public List<ProductDTO> getAllProducts() {
        logger.info("Fetching all products");
        return productDao.findAll().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }

    public List<Product> findProductsBySeller(String sellerID) {
        logger.info("Fetching products for sellerID: {}", sellerID);
        return productDao.findBySellerUserID(sellerID);
    }

    public List<Product> findProductsByAuction(String auctionID) {
        logger.info("Fetching products for auctionID: {}", auctionID);
        return productDao.findByAuctionAuctionID(auctionID);
    }

    public List<Product> findProductsByStatus(Product.ProductStatus status) {
        logger.info("Fetching products with status: {}", status);
        return productDao.findByProductStatus(status);
    }

    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.info("Fetching products with price range: {} to {}", minPrice, maxPrice);
        return productDao.findByPriceRange(minPrice, maxPrice);
    }
}