package com.bidpro.service;

import com.bidpro.dao.*;
import com.bidpro.dto.ProductDTO;
import com.bidpro.entities.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ProductService {
    private final ProductDao products;
    private final CategoryDao categories;
    private final BidDao bids;
    private final ProductImagesDao images;
    private final AuctionDao auctions;
    private final CurrentUserService current;
    private final NotificationService notifications;
    public ProductService(ProductDao products, CategoryDao categories, BidDao bids, ProductImagesDao images,
                          AuctionDao auctions, CurrentUserService current, NotificationService notifications) {
        this.products=products; this.categories=categories; this.bids=bids; this.images=images;
        this.auctions=auctions; this.current=current; this.notifications=notifications;
    }
    public ProductDTO createProduct(ProductDTO dto, String ignored) {
        current.requireRoles(User.Role.SELLER, User.Role.BOTH, User.Role.ADMIN);
        Product product=new Product();
        product.setProductID(UUID.randomUUID().toString());
        product.setSellerID(current.id());
        apply(product,dto);
        product.setProductStatus(Product.ProductStatus.PENDING);
        products.save(product);
        notifications.notifyUser(product.getSellerID(), "LISTING_SUBMITTED", "Your listing \"" + product.getTitle() + "\" was submitted for review.", "/seller");
        return toProductDTO(product);
    }
    private void apply(Product product, ProductDTO dto) {
        if (dto.getAuctionStartDate()==null || dto.getAuctionEndDate()==null ||
            !dto.getAuctionEndDate().isAfter(dto.getAuctionStartDate()) || !dto.getAuctionEndDate().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Auction end must be in the future and after its start");
        if (dto.getStartingPrice()==null || dto.getStartingPrice().signum()<=0 ||
            dto.getIncrementGap()==null || dto.getIncrementGap().signum()<=0 ||
            dto.getStartingPrice().scale()>2 || dto.getIncrementGap().scale()>2 ||
            dto.getStartingPrice().precision()-dto.getStartingPrice().scale()>8 ||
            dto.getIncrementGap().precision()-dto.getIncrementGap().scale()>8)
            throw new IllegalArgumentException("Starting price and increment must be positive with at most eight integer digits and two decimal places");
        var category=categories.findById(dto.getCategoryID()).orElseThrow(()->new EntityNotFoundException("Category not found"));
        product.setTitle(dto.getTitle().trim()); product.setDescription(dto.getDescription().trim());
        product.setCategoryID(category.getCategoryID()); product.setCategory(category);
        product.setAuctionStartDate(dto.getAuctionStartDate()); product.setAuctionEndDate(dto.getAuctionEndDate());
        product.setStartingPrice(dto.getStartingPrice()); product.setIncrementGap(dto.getIncrementGap());
        product.setProductCondition(dto.getProductCondition());
        product.setPromotionStatus(Product.PromotionStatus.NONE); product.setUpdatedAt(LocalDateTime.now());
    }
    public Product requireProduct(String id) {
        return products.findById(id).orElseThrow(()->new EntityNotFoundException("Product not found"));
    }
    public Product requireProductForUpdate(String id) {
        return products.findForUpdateByProductID(id).orElseThrow(()->new EntityNotFoundException("Product not found"));
    }
    public void requireOwner(Product product) {
        current.requireApproved();
        if (!current.isAdmin() && !current.id().equals(product.getSellerID()))
            throw new AccessDeniedException("You can manage only your own listings");
    }
    public ProductDTO findProductById(String id) {
        var product=requireProduct(id);
        if (!isPublic(product)) requireOwner(product);
        return toProductDTO(product);
    }
    private boolean isPublic(Product p) {
        return p.getProductStatus()==Product.ProductStatus.APPROVED || p.getProductStatus()==Product.ProductStatus.SOLD;
    }
    public List<ProductDTO> getPublicProducts() {
        return products.findAll().stream().filter(this::isPublic).map(this::toProductDTO).toList();
    }
    public List<ProductDTO> getAllProducts() {
        current.requireRoles(User.Role.ADMIN);
        return products.findAll().stream().map(this::toProductDTO).toList();
    }
    public List<ProductDTO> findMine() {
        current.requireApproved();
        return products.findBySellerUserID(current.id()).stream().map(this::toProductDTO).toList();
    }
    public List<ProductDTO> findProductsByCategory(String id) {
        return products.findByCategoryCategoryID(id).stream().filter(this::isPublic).map(this::toProductDTO).toList();
    }
    public List<ProductDTO> searchProducts(String keyword) {
        var query=keyword.toLowerCase(Locale.ROOT).trim();
        return getPublicProducts().stream().filter(p->p.getTitle().toLowerCase(Locale.ROOT).contains(query) ||
            p.getDescription().toLowerCase(Locale.ROOT).contains(query)).toList();
    }
    @Transactional(readOnly = true)
    public CatalogPage searchCatalog(String query, String category, String status, String sort, int page, int size) {
        if (page < 0 || size < 1 || size > 48 || (long) page * size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Use a non-negative page and a page size between 1 and 48");
        String term = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        String categoryID = category == null ? "" : category.strip();
        String state = status == null ? "" : status.strip().toUpperCase(Locale.ROOT);
        String order = sort == null || sort.isBlank() ? "ending" : sort.toLowerCase(Locale.ROOT);
        if (term.length() > 200 || categoryID.length() > 50)
            throw new IllegalArgumentException("Search text supports up to 200 characters and category IDs up to 50");
        if (!Set.of("", "LIVE", "UPCOMING", "ENDED", "SOLD").contains(state))
            throw new IllegalArgumentException("Choose Live, Upcoming, Ended or Sold");
        if (!Set.of("ending", "newest", "price-low", "price-high").contains(order))
            throw new IllegalArgumentException("Choose ending, newest, price-low or price-high sorting");
        var results = products.searchPublicCatalog(term, categoryID, state, order, LocalDateTime.now(), PageRequest.of(page, size));
        return new CatalogPage(results.getContent().stream().map(this::toProductDTO).toList(),
                results.getTotalElements(), results.getTotalPages(), page, size);
    }
    public record CatalogPage(List<ProductDTO> items, long totalItems, int totalPages, int page, int size) {}
    public ProductDTO updateProduct(String id, ProductDTO dto, String ignored) {
        var product=products.findForUpdateByProductID(id).orElseThrow(()->new EntityNotFoundException("Product not found"));
        requireOwner(product);
        if (product.getProductStatus()!=Product.ProductStatus.PENDING && product.getProductStatus()!=Product.ProductStatus.REJECTED)
            throw new IllegalArgumentException("Only pending or rejected listings can be edited");
        apply(product,dto); product.setProductStatus(Product.ProductStatus.PENDING);
        return toProductDTO(products.save(product));
    }
    public ProductDTO updateStatus(String id, Product.ProductStatus status) { return updateStatus(id, status.name()); }
    public ProductDTO updateStatus(String id, String status) {
        current.requireRoles(User.Role.ADMIN);
        var product=products.findForUpdateByProductID(id).orElseThrow(()->new EntityNotFoundException("Product not found"));
        Product.ProductStatus next=Product.ProductStatus.valueOf(status.toUpperCase(Locale.ROOT));
        if (next!=Product.ProductStatus.APPROVED && next!=Product.ProductStatus.REJECTED)
            throw new IllegalArgumentException("Choose APPROVED or REJECTED");
        if (product.getProductStatus()==next) return toProductDTO(product);
        if (product.getProductStatus()==Product.ProductStatus.SOLD || bids.existsByProductID(id))
            throw new IllegalArgumentException("A listing with bids cannot be moderated; close its auction instead");
        if (next==Product.ProductStatus.APPROVED) {
            if (!product.getAuctionEndDate().isAfter(LocalDateTime.now()))
                throw new IllegalArgumentException("The auction has expired; ask the seller to update its dates");
            Auction auction=product.getAuctionID()==null ? new Auction() : auctions.findById(product.getAuctionID()).orElseGet(Auction::new);
            if (auction.getAuctionID()==null) auction.setAuctionID(UUID.randomUUID().toString());
            auction.setTitle(product.getTitle()); auction.setProductID(id);
            auction.setStartDate(product.getAuctionStartDate()); auction.setEndDate(product.getAuctionEndDate());
            auction.setStartingPrice(product.getStartingPrice()); auction.setAuctionType(Auction.AuctionType.STANDARD);
            auction.setStatus(LocalDateTime.now().isBefore(auction.getStartDate()) ? Auction.Status.UPCOMING : Auction.Status.ONGOING);
            auction.setCreatedBy(current.id()); auctions.save(auction); product.setAuctionID(auction.getAuctionID());
        }
        product.setProductStatus(next); product.setUpdatedAt(LocalDateTime.now()); products.save(product);
        notifications.notifyUser(product.getSellerID(), "LISTING_STATUS", "Your listing \"" + product.getTitle() + "\" is " + next.name().toLowerCase() + ".", "/seller");
        return toProductDTO(product);
    }
    public void deleteProduct(String id) {
        var product=requireProductForUpdate(id); requireOwner(product);
        if (product.getProductStatus()!=Product.ProductStatus.PENDING && product.getProductStatus()!=Product.ProductStatus.REJECTED)
            throw new IllegalArgumentException("Only pending or rejected listings can be deleted");
        // Rejected listings may retain an auction record from moderation; preserve its history.
        if (product.getAuctionID()!=null) throw new IllegalArgumentException("This listing has auction history and cannot be deleted");
        images.deleteAll(images.findByProductProductID(id)); products.delete(product);
    }
    public ProductDTO toProductDTO(Product product) {
        ProductDTO dto=new ProductDTO(); dto.setProductID(product.getProductID()); dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription()); dto.setSellerID(product.getSellerID());
        dto.setCategoryID(product.getCategoryID());
        dto.setCategoryName(categories.findById(product.getCategoryID()).map(Category::getName).orElse("Uncategorized"));
        dto.setAuctionID(product.getAuctionID()); dto.setProductStatus(product.getProductStatus());
        dto.setAuctionStartDate(product.getAuctionStartDate()); dto.setAuctionEndDate(product.getAuctionEndDate());
        dto.setStartingPrice(product.getStartingPrice()); dto.setIncrementGap(product.getIncrementGap());
        dto.setProductCondition(product.getProductCondition()); dto.setPromotionStatus(product.getPromotionStatus());
        dto.setCreatedAt(product.getCreatedAt()); dto.setUpdatedAt(product.getUpdatedAt());
        var summary=bids.summarizeByProductID(product.getProductID());
        dto.setBidCount(summary.getBidCount());
        dto.setCurrentBid(summary.getHighestAmount()==null?product.getStartingPrice():summary.getHighestAmount());
        dto.setImageUrls(images.findByProductProductID(product.getProductID()).stream().map(ProductImages::getImageUrl).toList());
        return dto;
    }
    public List<Product> findProductsBySeller(String id) { return products.findBySellerUserID(id); }
    public List<Product> findProductsByAuction(String id) { return products.findByAuctionAuctionID(id); }
    public List<Product> findProductsByStatus(Product.ProductStatus status) { return products.findByProductStatus(status); }
    public List<Product> findProductsByPriceRange(BigDecimal min, BigDecimal max) { return products.findByPriceRange(min,max); }
}
