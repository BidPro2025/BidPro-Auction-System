package com.bidpro.dao;

import com.bidpro.entities.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BidDao extends JpaRepository<Bid, String> {
    List<Bid> findByProductProductID(String productID);
    List<Bid> findByBidderUserID(String bidderID);
    @Query("SELECT b FROM Bid b WHERE b.product.productID = :productID ORDER BY b.amount DESC")
    List<Bid> findHighestBidByProduct(@Param("productID") String productID);
    @Query("SELECT b FROM Bid b WHERE b.timestamp BETWEEN :startDate AND :endDate")
    List<Bid> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                             @Param("endDate") LocalDateTime endDate);
}