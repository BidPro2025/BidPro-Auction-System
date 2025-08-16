
package com.bidpro.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bidpro.dto.AdminDTO;
import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.AuctionDTO;
import com.bidpro.dto.CategoryDTO;
import com.bidpro.dto.LoginDTO;
import com.bidpro.dto.ProductDTO;
import com.bidpro.dto.SignupDTO;
import com.bidpro.dto.UserDTO;
import com.bidpro.entities.Auction;
import com.bidpro.entities.Bid;
import com.bidpro.entities.Invoice;
import com.bidpro.entities.User;
import com.bidpro.service.AdminService;
import com.bidpro.service.AuctionService;
import com.bidpro.service.BidService;
import com.bidpro.service.CategoryService;
import com.bidpro.service.InvoiceService;
import com.bidpro.service.ProductService;
import com.bidpro.service.UserService;
import com.bidpro.util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private BidService bidService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminDTO>> createAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        try {
            logger.info("Creating admin");
            AdminDTO createdAdmin = adminService.createAdmin(adminDTO, "system");
            return ResponseEntity.ok(ApiResponse.success(createdAdmin, "Admin created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create admin: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "EMAIL_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating admin: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to create admin: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{adminID}")
    public ResponseEntity<ApiResponse<AdminDTO>> getAdminById(@PathVariable String adminID) {
        try {
            logger.info("Fetching admin with ID: {}", adminID);
            AdminDTO admin = adminService.getAdminById(adminID);
            return ResponseEntity.ok(ApiResponse.success(admin, "Admin retrieved successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Admin not found: {}", adminID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "ADMIN_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving admin with ID: {}", adminID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to retrieve admin: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<AdminDTO>> getAdminByEmail(@PathVariable String email) {
        try {
            logger.info("Fetching admin with email: {}", email);
            AdminDTO admin = adminService.getAdminByEmail(email.toLowerCase());
            return ResponseEntity.ok(ApiResponse.success(admin, "Admin retrieved successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Admin not found with email: {}", email);
            return new ResponseEntity<>(ApiResponse.error("Admin not found", "ADMIN_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving admin with email: {}", email, e);
            return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{adminID}")
    public ResponseEntity<ApiResponse<AdminDTO>> updateAdmin(@PathVariable String adminID, @Valid @RequestBody AdminDTO adminDTO) {
        try {
            logger.info("Updating admin with ID: {}", adminID);
            AdminDTO updatedAdmin = adminService.updateAdmin(adminID, adminDTO, "system");
            return ResponseEntity.ok(ApiResponse.success(updatedAdmin, "Admin updated successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Admin not found: {}", adminID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "ADMIN_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update admin: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "EMAIL_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating admin with ID: {}", adminID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to update admin: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            logger.info("Admin login attempt for email: {}", loginDTO.getEmail());
            AdminDTO adminDTO = adminService.login(loginDTO);
            String token = jwtUtil.generateToken(adminDTO.getEmail(), "ADMIN");

            Map<String, Object> response = new HashMap<>();
            response.put("admin", adminDTO);
            response.put("token", token);

            return ResponseEntity.ok(ApiResponse.success(response, "Admin logged in successfully"));
        } catch (RuntimeException e) {
            logger.warn("Login failed: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "INVALID_CREDENTIALS"), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Error during admin login: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to login: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        try {
            return ResponseEntity.ok(ApiResponse.success(null, "Admin logged out successfully"));
        } catch (Exception e) {
            logger.error("Error during admin logout: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to logout: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardData>> getDashboardData() {
        try {
            List<UserDTO> users = userService.findAllUsers();
            int totalUsers = users.size();

            List<Auction> activeAuctions = auctionService.findAuctionsByStatus(Auction.Status.ONGOING);
            int totalActiveAuctions = activeAuctions.size();

            List<Bid> allBids = bidService.findBidsByProduct(null);
            int totalBids = allBids.size();

            BigDecimal revenue = invoiceService.findInvoicesByPaymentStatus(Invoice.PaymentStatus.PAID)
                    .stream()
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<UserDTO> recentUsers = users.stream()
                    .sorted((u1, u2) -> u2.getCreatedAt() != null && u1.getCreatedAt() != null ?
                            u2.getCreatedAt().compareTo(u1.getCreatedAt()) : 0)
                    .limit(5)
                    .collect(Collectors.toList());

            List<ProductDTO> recentProducts = productService.getAllProducts().stream()
                    .sorted((p1, p2) -> p2.getCreatedAt() != null && p1.getCreatedAt() != null ?
                            p2.getCreatedAt().compareTo(p1.getCreatedAt()) : 0)
                    .limit(5)
                    .collect(Collectors.toList());

            DashboardData data = new DashboardData(
                    totalUsers,
                    totalActiveAuctions,
                    totalBids,
                    revenue,
                    recentUsers,
                    activeAuctions,
                    recentProducts
            );

            return ResponseEntity.ok(ApiResponse.success(data, "Dashboard data retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching dashboard data: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch dashboard data: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<UserDTO> users = userService.findAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch users: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody SignupDTO signupDTO) {
        try {
            UserDTO createdUser = userService.signup(signupDTO);
            return ResponseEntity.ok(ApiResponse.success(createdUser, "User created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create user: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "EMAIL_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to create user: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/users/{userID}/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(@PathVariable String userID, @RequestBody Map<String, String> statusUpdate) {
        try {
            UserDTO userDTO = userService.findUserById(userID);
            userDTO.setStatus(User.Status.valueOf(statusUpdate.get("status")));
            UserDTO updatedUser = userService.updateUser(userID, userDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "User status updated successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("User not found: {}", userID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "USER_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "INVALID_STATUS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating user status: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to update user status: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/auctions")
    public ResponseEntity<ApiResponse<List<AuctionDTO>>> getAllAuctions() {
        try {
            List<AuctionDTO> auctions = auctionService.getAllAuctions();
            return ResponseEntity.ok(ApiResponse.success(auctions, "Auctions retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching auctions: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch auctions: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/auctions")
    public ResponseEntity<ApiResponse<AuctionDTO>> createAuction(@Valid @RequestBody AuctionDTO auctionDTO) {
        try {
            AuctionDTO createdAuction = auctionService.createAuction(auctionDTO, "system");
            return ResponseEntity.ok(ApiResponse.success(createdAuction, "Auction created successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to create auction: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "ENTITY_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error creating auction: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to create auction: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch categories: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/categories/{categoryID}")
    public ResponseEntity<ApiResponse<CategoryDTO>> findCategoryById(@PathVariable String categoryID) {
        try {
            logger.info("Fetching category with ID: {}", categoryID);
            CategoryDTO category = categoryService.getCategoryById(categoryID);
            return ResponseEntity.ok(ApiResponse.success(category, "Category retrieved successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Category not found: {}", categoryID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving category with ID: {}", categoryID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to retrieve category: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/categories/visible")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> findVisibleCategories() {
        try {
            logger.info("Fetching visible categories");
            List<CategoryDTO> categories = categoryService.getVisibleCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "Visible categories retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching visible categories: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch visible categories: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            return ResponseEntity.ok(ApiResponse.success(createdCategory, "Category created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create category: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to create category: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/categories/{categoryID}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(@PathVariable String categoryID, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(categoryID, categoryDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedCategory, "Category updated successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Category not found: {}", categoryID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update category: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating category with ID: {}", categoryID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to update category: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/categories/{categoryID}/visibility")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategoryVisibility(@PathVariable String categoryID, @RequestBody Map<String, Boolean> visibilityUpdate) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategoryVisibility(categoryID, visibilityUpdate.get("visibility"));
            return ResponseEntity.ok(ApiResponse.success(updatedCategory, "Category visibility updated successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Category not found: {}", categoryID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid visibility: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "INVALID_VISIBILITY"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating category visibility: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to update category visibility: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/categories/{categoryID}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable String categoryID) {
        try {
            categoryService.deleteCategory(categoryID);
            return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Category not found: {}", categoryID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "CATEGORY_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete category: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "INVALID_OPERATION"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error deleting category with ID: {}", categoryID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to delete category: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to fetch products: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO, "system");
            return ResponseEntity.ok(ApiResponse.success(createdProduct, "Product created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create product: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "PRODUCT_ALREADY_EXISTS"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return new ResponseEntity<>(ApiResponse.error("Failed to create product: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/products/{productID}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable String productID, @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(productID, productDTO, "system");
            return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Product not found: {}", productID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "PRODUCT_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update product: {}", e.getMessage());
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "INVALID_INPUT"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating product with ID: {}", productID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to update product: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/products/{productID}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productID) {
        try {
            productService.deleteProduct(productID);
            return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
        } catch (EntityNotFoundException e) {
            logger.warn("Product not found: {}", productID);
            return new ResponseEntity<>(ApiResponse.error(e.getMessage(), "PRODUCT_NOT_FOUND"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error deleting product with ID: {}", productID, e);
            return new ResponseEntity<>(ApiResponse.error("Failed to delete product: " + e.getMessage(), "INTERNAL_ERROR"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static class DashboardData {
        private int totalUsers;
        private int activeAuctionsCount;
        private int totalBids;
        private BigDecimal revenue;
        private List<UserDTO> recentUsers;
        private List<Auction> activeAuctions;
        private List<ProductDTO> recentProducts;

        public DashboardData(int totalUsers, int activeAuctionsCount, int totalBids, BigDecimal revenue,
                            List<UserDTO> recentUsers, List<Auction> activeAuctions, List<ProductDTO> recentProducts) {
            this.totalUsers = totalUsers;
            this.activeAuctionsCount = activeAuctionsCount;
            this.totalBids = totalBids;
            this.revenue = revenue;
            this.recentUsers = recentUsers;
            this.activeAuctions = activeAuctions;
            this.recentProducts = recentProducts;
        }

        public int getTotalUsers() { return totalUsers; }
        public int getActiveAuctionsCount() { return activeAuctionsCount; }
        public int getTotalBids() { return totalBids; }
        public BigDecimal getRevenue() { return revenue; }
        public List<UserDTO> getRecentUsers() { return recentUsers; }
        public List<Auction> getActiveAuctions() { return activeAuctions; }
        public List<ProductDTO> getRecentProducts() { return recentProducts; }
    }
}
