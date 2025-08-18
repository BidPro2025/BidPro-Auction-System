package com.bidpro.dao;

import com.bidpro.entities.AuctionRegistration;
import com.bidpro.entities.AuctionRegistration.RegistrationStatus; // Correct import for the inner enum
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuctionRegistrationDao extends JpaRepository<AuctionRegistration, AuctionRegistration.AuctionRegistrationId> {
    List<AuctionRegistration> findByAuctionAuctionID(String auctionID);
    List<AuctionRegistration> findByUserUserID(String userID);
    List<AuctionRegistration> findByRegistrationStatus(RegistrationStatus status);
}