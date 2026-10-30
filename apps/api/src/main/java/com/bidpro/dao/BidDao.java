package com.bidpro.dao;

import com.bidpro.entities.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BidDao extends JpaRepository<Bid, String> {
    List<Bid> findByProductProductID(String productID);
    List<Bid> findByBidderUserID(String bidderID);
    List<Bid> findByProductIDOrderByAmountDescTimestampAsc(String productID);
    List<Bid> findByBidderIDOrderByTimestampDesc(String bidderID);
    Optional<Bid> findFirstByProductIDOrderByAmountDescTimestampAsc(String productID);
    boolean existsByProductID(String productID);
    @Query("select count(b) as bidCount, max(b.amount) as highestAmount from Bid b where b.productID = :productID")
    Summary summarizeByProductID(@Param("productID") String productID);
    interface Summary {
        long getBidCount();
        BigDecimal getHighestAmount();
    }
    @Query("select b.productID from Bid b where b.bidID = :bidID")
    Optional<String> findProductIDByBidID(@Param("bidID") String bidID);
    @Query("select b from Bid b where b.productID = :productID order by b.amount desc, b.timestamp asc")
    List<Bid> findHighestBidByProduct(@Param("productID") String productID);
    @Query("select b from Bid b where b.timestamp between :startDate and :endDate")
    List<Bid> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
