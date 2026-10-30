package com.bidpro.dao;

import com.bidpro.entities.Auction;
import com.bidpro.entities.Auction.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionDao extends JpaRepository<Auction, String> {
    @Query("select a.productID from Auction a where a.auctionID = :auctionID")
    Optional<String> findProductIDByAuctionID(@Param("auctionID") String auctionID);
    List<Auction> findByCreatedBy(String adminID);

    @Query("select a from Auction a where a.startDate >= :start and a.endDate <= :end")
    List<Auction> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Auction> findByStatus(Status status);
}
