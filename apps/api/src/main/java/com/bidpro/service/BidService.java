package com.bidpro.service;

import com.bidpro.dao.BidDao;
import com.bidpro.dao.ProductDao;
import com.bidpro.dto.BidDTO;
import com.bidpro.entities.Bid;
import com.bidpro.entities.Product;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BidService {
    private final BidDao bids;
    private final ProductDao products;
    private final CurrentUserService current;
    private final NotificationService notifications;
    private final LogService audit;

    public BidService(BidDao bids, ProductDao products, CurrentUserService current, NotificationService notifications, LogService audit) {
        this.bids = bids;
        this.products = products;
        this.current = current;
        this.notifications = notifications;
        this.audit = audit;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BidDTO placeBid(BidDTO dto) {
        current.requireRoles(User.Role.BIDDER, User.Role.BOTH);
        if (dto.getProductID() == null || dto.getProductID().isBlank()) throw new IllegalArgumentException("Product ID is mandatory");
        Product product = lockedProduct(dto.getProductID());
        validateBid(product, dto.getAmount());
        var previousLeader = bids.findFirstByProductIDOrderByAmountDescTimestampAsc(product.getProductID());
        Bid bid = new Bid();
        bid.setBidID(UUID.randomUUID().toString());
        bid.setProductID(product.getProductID());
        bid.setBidderID(current.id());
        bid.setAmount(dto.getAmount().setScale(2, RoundingMode.UNNECESSARY));
        bid.setTimestamp(LocalDateTime.now());
        bids.save(bid);
        audit.logAction("BID", bid.getBidID(), "ACCEPTED", current.id(),
                "product=" + product.getProductID() + ";amountINR=" + bid.getAmount().toPlainString());
        notifyBid(product, bid, previousLeader.map(Bid::getBidderID).orElse(null));
        return toDTO(bid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BidDTO updateBid(String bidID, BidDTO dto) {
        current.requireRoles(User.Role.BIDDER, User.Role.BOTH);
        // Obtain the product identifier without caching a bid before its lock.
        String productID = bids.findProductIDByBidID(bidID).orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        Product product = lockedProduct(productID);
        Bid original = bids.findById(bidID).orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        if (!original.getBidderID().equals(current.id())) throw new AccessDeniedException("You can only increase your own bids");
        if (dto.getProductID() != null && !productID.equals(dto.getProductID())) throw new IllegalArgumentException("The product of a bid cannot be changed");
        validateBid(product, dto.getAmount());
        var previousLeader = bids.findFirstByProductIDOrderByAmountDescTimestampAsc(productID);
        // An increase is a new accepted bid. Retain the original amount and
        // timestamp so bidding history cannot be rewritten after acceptance.
        Bid bid = new Bid();
        bid.setBidID(UUID.randomUUID().toString());
        bid.setProductID(productID);
        bid.setBidderID(current.id());
        bid.setAmount(dto.getAmount().setScale(2, RoundingMode.UNNECESSARY));
        bid.setTimestamp(LocalDateTime.now());
        bids.save(bid);
        audit.logAction("BID", bid.getBidID(), "INCREASE_ACCEPTED", current.id(),
                "product=" + productID + ";previousBid=" + original.getBidID() + ";amountINR=" + bid.getAmount().toPlainString());
        notifyBid(product, bid, previousLeader.map(Bid::getBidderID).orElse(null));
        return toDTO(bid);
    }

    private void notifyBid(Product product, Bid bid, String previousBidderID) {
        String link = "/products/" + product.getProductID();
        notifications.notifyUser(bid.getBidderID(), "BID_PLACED", "Your bid of INR " + bid.getAmount().toPlainString() + " on " + product.getTitle() + " was accepted.", link);
        notifications.notifyUser(product.getSellerID(), "NEW_BID", "Your listing " + product.getTitle() + " received a bid of INR " + bid.getAmount().toPlainString() + ".", link);
        if (previousBidderID != null && !previousBidderID.equals(bid.getBidderID()))
            notifications.notifyUser(previousBidderID, "OUTBID", "You have been outbid on " + product.getTitle() + ".", link);
    }
    private Product lockedProduct(String productID) {
        return products.findForUpdateByProductID(productID).orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    private void validateBid(Product product, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2 || amount.precision() - amount.scale() > 8)
            throw new IllegalArgumentException("Enter a positive bid with at most two decimal places");
        if (product.getSellerID().equals(current.id())) throw new AccessDeniedException("You cannot bid on your own listing");
        LocalDateTime now = LocalDateTime.now();
        if (product.getProductStatus() != Product.ProductStatus.APPROVED) throw new IllegalArgumentException("This listing is not available for bidding");
        if (product.getAuctionStartDate() == null || product.getAuctionEndDate() == null || now.isBefore(product.getAuctionStartDate()) || !now.isBefore(product.getAuctionEndDate()))
            throw new IllegalArgumentException("Bids are accepted only while the auction is open");
        BigDecimal minimum = bids.findFirstByProductIDOrderByAmountDescTimestampAsc(product.getProductID())
                .map(bid -> bid.getAmount().add(product.getIncrementGap())).orElse(product.getStartingPrice());
        if (amount.compareTo(minimum) < 0) throw new IllegalArgumentException("Minimum bid is " + minimum.toPlainString());
    }

    public List<BidDTO> ownBids() {
        current.requireApproved();
        return (current.isAdmin() ? bids.findAll() : bids.findByBidderIDOrderByTimestampDesc(current.id())).stream().map(this::toDTO).toList();
    }

    public List<Bid> findBidsByProduct(String productID) {
        if (productID == null) return bids.findAll();
        requirePublishedProduct(productID);
        return bids.findByProductIDOrderByAmountDescTimestampAsc(productID);
    }
    public List<Bid> findBidsByBidder(String bidderID) {
        current.requireApproved();
        if (!current.isAdmin() && !current.id().equals(bidderID)) throw new AccessDeniedException("You can only view your own bid history");
        return bids.findByBidderIDOrderByTimestampDesc(bidderID);
    }
    public List<Bid> findHighestBidByProduct(String productID) {
        requirePublishedProduct(productID);
        return bids.findFirstByProductIDOrderByAmountDescTimestampAsc(productID).stream().toList();
    }
    private void requirePublishedProduct(String productID) {
        Product product = products.findById(productID).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (product.getProductStatus() != Product.ProductStatus.APPROVED && product.getProductStatus() != Product.ProductStatus.SOLD)
            throw new EntityNotFoundException("Product not found");
    }
    public BidDTO toDTO(Bid bid) {
        BidDTO dto = new BidDTO(bid);
        products.findById(bid.getProductID()).ifPresent(product -> dto.setProductTitle(product.getTitle()));
        return dto;
    }
}
