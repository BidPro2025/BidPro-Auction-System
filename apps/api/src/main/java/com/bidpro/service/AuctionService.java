package com.bidpro.service;
import com.bidpro.dao.*;
import com.bidpro.dto.AuctionDTO;
import com.bidpro.entities.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional(isolation = Isolation.READ_COMMITTED)
public class AuctionService {
    private final AuctionDao auctions;
    private final ProductDao products;
    private final AuctionRegistrationDao registrations;
    private final CurrentUserService current;
    private final LogService audit;
    public AuctionService(AuctionDao auctions, ProductDao products, AuctionRegistrationDao registrations, CurrentUserService current, LogService audit) {
        this.auctions=auctions;this.products=products;this.registrations=registrations;this.current=current;this.audit=audit;
    }
    public AuctionDTO createAuction(AuctionDTO dto,String ignored) {
        current.requireRoles(User.Role.ADMIN);
        var product=products.findForUpdateByProductID(dto.getProductID()).orElseThrow(()->new EntityNotFoundException("Product not found"));
        if (product.getProductStatus()!=Product.ProductStatus.APPROVED) throw new IllegalArgumentException("Approve the listing first");
        if (product.getAuctionID()!=null) throw new IllegalArgumentException("The product already has an auction");
        if (dto.getStartDate()==null || dto.getEndDate()==null || !dto.getEndDate().isAfter(dto.getStartDate()) || !dto.getEndDate().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Auction dates are invalid");
        if (dto.getAuctionType()==Auction.AuctionType.REVERSE) throw new IllegalArgumentException("Only standard auctions are supported");
        var auction=new Auction();auction.setAuctionID(UUID.randomUUID().toString());auction.setProductID(product.getProductID());
        auction.setTitle(dto.getTitle());auction.setStartDate(dto.getStartDate());auction.setEndDate(dto.getEndDate());
        auction.setStartingPrice(product.getStartingPrice());auction.setAuctionType(Auction.AuctionType.STANDARD);auction.setCreatedBy(current.id());
        auction.setStatus(status(auction));auctions.save(auction);
        product.setAuctionID(auction.getAuctionID());product.setAuctionStartDate(auction.getStartDate());product.setAuctionEndDate(auction.getEndDate());products.save(product);
        return toDTO(auction);
    }
    private Auction require(String id) { return auctions.findById(id).orElseThrow(()->new EntityNotFoundException("Auction not found")); }
    private Auction.Status status(Auction a) {
        var now=LocalDateTime.now();return !now.isBefore(a.getEndDate())?Auction.Status.PAST:now.isBefore(a.getStartDate())?Auction.Status.UPCOMING:Auction.Status.ONGOING;
    }
    private boolean visible(Auction a) {
        return products.findById(a.getProductID()).map(p->p.getProductStatus()==Product.ProductStatus.APPROVED || p.getProductStatus()==Product.ProductStatus.SOLD).orElse(false);
    }
    private AuctionDTO toDTO(Auction a) { var dto=new AuctionDTO(a);dto.setStatus(status(a));return dto; }
    public AuctionDTO findAuctionById(String id) {
        var a=require(id);if (!visible(a)) throw new EntityNotFoundException("Auction not found");return toDTO(a);
    }
    public List<AuctionDTO> getAllAuctions() { return auctions.findAll().stream().filter(this::visible).map(this::toDTO).toList(); }
    public List<AuctionDTO> findAllAuctions() { return getAllAuctions(); }
    public List<AuctionDTO> listByStatus(String status) {
        var desired=Auction.Status.valueOf(status.toUpperCase(Locale.ROOT));return getAllAuctions().stream().filter(a->a.getStatus()==desired).toList();
    }
    public List<Auction> findAuctionsByStatus(Auction.Status desired) { return auctions.findAll().stream().filter(this::visible).filter(a->status(a)==desired).toList(); }
    public List<Auction> findAuctionsByDateRange(LocalDateTime start,LocalDateTime end) { return auctions.findByDateRange(start,end); }
    public List<Auction> findAuctionsByAdmin(String id) { return auctions.findByCreatedBy(id); }
    public void registerForAuction(String id,String ignored) {
        current.requireRoles(User.Role.BIDDER,User.Role.BOTH);
        var a=require(id);var product=products.findForUpdateByProductID(a.getProductID()).orElseThrow(()->new EntityNotFoundException("Product not found"));
        if (!visible(a) || product.getAuctionEndDate()==null || !LocalDateTime.now().isBefore(product.getAuctionEndDate())) throw new IllegalArgumentException("This auction is no longer open for registration");
        if (current.id().equals(product.getSellerID())) throw new IllegalArgumentException("You cannot register for your own auction");
        var key=new AuctionRegistration.AuctionRegistrationId();key.setAuctionID(id);key.setUserID(current.id());
        if (registrations.existsById(key)) return;
        var registration=new AuctionRegistration();registration.setId(key);registration.setAuction(a);registration.setUser(current.user());
        registration.setRegistrationStatus(AuctionRegistration.RegistrationStatus.APPROVED);registration.setRegisteredAt(LocalDateTime.now());registrations.save(registration);
    }
    public AuctionDTO close(String id) {
        current.requireRoles(User.Role.ADMIN);
        String productID=auctions.findProductIDByAuctionID(id).orElseThrow(()->new EntityNotFoundException("Auction not found"));
        var product=products.findForUpdateByProductID(productID).orElseThrow(()->new EntityNotFoundException("Product not found"));
        var a=require(id);
        if (product.getProductStatus()==Product.ProductStatus.SOLD) throw new IllegalArgumentException("This auction is already settled");
        var now=LocalDateTime.now();
        if (now.isBefore(a.getStartDate())) throw new IllegalArgumentException("An upcoming auction cannot be closed before its start");
        if (a.getEndDate().isAfter(now)) {
            a.setEndDate(now);product.setAuctionEndDate(now);products.save(product);
            audit.logAction("AUCTION",id,"CLOSED_EARLY",current.id(),"product="+productID);
        }
        a.setStatus(Auction.Status.PAST);a.setUpdatedAt(now);auctions.save(a);return toDTO(a);
    }
}
