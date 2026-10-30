package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.entities.*;
import com.bidpro.service.*;
import com.bidpro.util.JwtUtil;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final UserService users; private final ProductService products; private final CategoryService categories;
    private final AuctionService auctions; private final BidService bids; private final InvoiceService invoices; private final JwtUtil jwt;
    public AdminController(UserService users, ProductService products, CategoryService categories, AuctionService auctions, BidService bids, InvoiceService invoices, JwtUtil jwt) {
        this.users=users; this.products=products; this.categories=categories; this.auctions=auctions; this.bids=bids; this.invoices=invoices; this.jwt=jwt;
    }
    @PostMapping("/login") public ApiResponse<Map<String,Object>> login(@Valid @RequestBody SigninDTO input) {
        UserDTO user=users.signin(input); if(user.getRole()!=User.Role.ADMIN) throw new AccessDeniedException("An administrator account is required");
        return ApiResponse.success(Map.of("user",user,"admin",user,"token",jwt.generateToken(user.getEmail(),"ADMIN",user.getSessionVersion())),"Signed in successfully");
    }
    @GetMapping("/users") public ApiResponse<List<UserDTO>> users() { return ApiResponse.success(users.findAllUsers(),"Users retrieved"); }
    @PostMapping("/users") public ApiResponse<UserDTO> createUser(@Valid @RequestBody SignupDTO input) { return ApiResponse.success(users.signup(input),"User created, awaiting approval"); }
    @PutMapping("/users/{id}/status") public ApiResponse<UserDTO> userStatus(@PathVariable String id,@Valid @RequestBody UserStatusRequest input) {
        return ApiResponse.success(users.updateStatus(id,input.status()),"User status updated");
    }
    @GetMapping("/products") public ApiResponse<List<ProductDTO>> products() { return ApiResponse.success(products.getAllProducts(),"Products retrieved"); }
    @PutMapping("/products/{id}/status") public ApiResponse<ProductDTO> productStatus(@PathVariable String id,@Valid @RequestBody ProductStatusRequest input) {
        return ApiResponse.success(products.updateStatus(id,input.status()),"Product status updated");
    }
    @GetMapping("/auctions") public ApiResponse<List<AuctionDTO>> auctions() { return ApiResponse.success(auctions.getAllAuctions(),"Auctions retrieved"); }
    @GetMapping("/bids") public ApiResponse<List<BidDTO>> bids() { return ApiResponse.success(bids.findBidsByProduct(null).stream().map(BidDTO::new).toList(),"Bids retrieved"); }
    @GetMapping("/categories") public ApiResponse<List<CategoryDTO>> categories() { return ApiResponse.success(categories.getAllCategories(),"Categories retrieved"); }
    @GetMapping("/categories/visible") public ApiResponse<List<CategoryDTO>> visibleCategories() { return ApiResponse.success(categories.getVisibleCategories(),"Categories retrieved"); }
    @GetMapping("/categories/{id}") public ApiResponse<CategoryDTO> category(@PathVariable String id) { return ApiResponse.success(categories.getCategoryById(id),"Category retrieved"); }
    @PostMapping("/categories") public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO input) { return ApiResponse.success(categories.createCategory(input),"Category created"); }
    @PutMapping("/categories/{id}") public ApiResponse<CategoryDTO> updateCategory(@PathVariable String id,@Valid @RequestBody CategoryDTO input) { return ApiResponse.success(categories.updateCategory(id,input),"Category updated"); }
    @PutMapping("/categories/{id}/visibility") public ApiResponse<CategoryDTO> visibility(@PathVariable String id,@Valid @RequestBody VisibilityRequest input) { return ApiResponse.success(categories.updateCategoryVisibility(id,input.visibility()),"Visibility updated"); }
    @DeleteMapping("/categories/{id}") public ApiResponse<Void> deleteCategory(@PathVariable String id) { categories.deleteCategory(id); return ApiResponse.success(null,"Category deleted"); }
    @GetMapping("/dashboard") public ApiResponse<Map<String,Object>> dashboard() {
        var allProducts=products.getAllProducts(); var allUsers=users.findAllUsers(); var allAuctions=auctions.getAllAuctions();
        BigDecimal revenue=invoices.findInvoicesByPaymentStatus(Invoice.PaymentStatus.PAID).stream().map(Invoice::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        return ApiResponse.success(Map.of("totalUsers",allUsers.size(),"totalProducts",allProducts.size(),
            "totalActiveAuctions",allAuctions.stream().filter(a->a.getStatus()==Auction.Status.ONGOING).count(),
            "totalBids",bids.findBidsByProduct(null).size(),"revenue",revenue,"recentUsers",allUsers.stream().limit(5).toList(),
            "recentProducts",allProducts.stream().limit(5).toList(),"activeAuctions",allAuctions.stream().filter(a->a.getStatus()==Auction.Status.ONGOING).toList()),"Dashboard retrieved");
    }
    public record UserStatusRequest(@jakarta.validation.constraints.NotNull User.Status status) {}
    public record ProductStatusRequest(@jakarta.validation.constraints.NotNull Product.ProductStatus status) {}
    public record VisibilityRequest(@jakarta.validation.constraints.NotNull Boolean visibility) {}
}
