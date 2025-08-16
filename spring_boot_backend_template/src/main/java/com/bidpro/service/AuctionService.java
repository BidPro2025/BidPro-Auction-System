package com.bidpro.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidpro.dao.AdminDao;
import com.bidpro.dao.AuctionDao;
import com.bidpro.dao.AuctionRegistrationDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.AuctionDTO;
import com.bidpro.entities.Admin;
import com.bidpro.entities.Auction;
import com.bidpro.entities.AuctionRegistration;
import com.bidpro.entities.User;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AuctionService {

    @Autowired
    private AuctionDao auctionDao;

    @Autowired
    private AuctionRegistrationDao auctionRegistrationDao;

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private UserDao userDao;

    public AuctionDTO createAuction(AuctionDTO auctionDTO, String adminID) {
        Admin admin = adminDao.findById(adminID)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + adminID));
        Auction auction = new Auction();
        auction.setAuctionID(UUID.randomUUID().toString());
        auction.setTitle(auctionDTO.getTitle());
        auction.setProductID(auctionDTO.getProductID());
        auction.setStartDate(auctionDTO.getStartDate());
        auction.setEndDate(auctionDTO.getEndDate());
        auction.setStartingPrice(auctionDTO.getStartingPrice());
        auction.setStatus(Auction.Status.valueOf(auctionDTO.getStatus().name()));
        auction.setAuctionType(Auction.AuctionType.valueOf(auctionDTO.getAuctionType().name()));
        auction.setCreatedBy(admin.getAdminId());
        auction = auctionDao.save(auction);
        auctionDTO.setAuctionID(auction.getAuctionID());
        auctionDTO.setCreatedBy(adminID);
        return auctionDTO;
    }

    public AuctionDTO findAuctionById(String auctionID) {
        Auction auction = auctionDao.findById(auctionID)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + auctionID));
        AuctionDTO dto = new AuctionDTO();
        dto.setAuctionID(auction.getAuctionID());
        dto.setTitle(auction.getTitle());
        dto.setProductID(auction.getProductID());
        dto.setStartDate(auction.getStartDate());
        dto.setEndDate(auction.getEndDate());
        dto.setStartingPrice(auction.getStartingPrice());
        dto.setStatus(auction.getStatus());
        dto.setAuctionType(auction.getAuctionType());
        dto.setCreatedBy(auction.getCreatedBy());
        return dto;
    }

    public void registerForAuction(String auctionID, String userID) {
        Auction auction = auctionDao.findById(auctionID)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + auctionID));
        User user = userDao.findById(userID)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userID));
        AuctionRegistration registration = new AuctionRegistration();
        AuctionRegistration.AuctionRegistrationId id = new AuctionRegistration.AuctionRegistrationId();
        id.setAuctionID(auctionID);
        id.setUserID(userID);
        registration.setId(id);
        registration.setAuction(auction);
        registration.setUser(user);
        registration.setRegisteredAt(LocalDateTime.now());
        auctionRegistrationDao.save(registration);
    }

    public List<Auction> findAuctionsByStatus(Auction.Status status) {
        return auctionDao.findByStatus(status);
    }

    public List<Auction> findAuctionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auctionDao.findByDateRange(startDate, endDate);
    }

    public List<Auction> findAuctionsByAdmin(String adminID) {
        return auctionDao.findByCreatedBy(adminID);
    }

    public List<AuctionDTO> getAllAuctions() {
        return auctionDao.findAll().stream().map(auction -> {
            AuctionDTO dto = new AuctionDTO();
            dto.setAuctionID(auction.getAuctionID());
            dto.setTitle(auction.getTitle());
            dto.setProductID(auction.getProductID());
            dto.setStartDate(auction.getStartDate());
            dto.setEndDate(auction.getEndDate());
            dto.setStartingPrice(auction.getStartingPrice());
            dto.setStatus(auction.getStatus());
            dto.setAuctionType(auction.getAuctionType());
            dto.setCreatedBy(auction.getCreatedBy());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<AuctionDTO> findAllAuctions() {
        return getAllAuctions();
    }
}
