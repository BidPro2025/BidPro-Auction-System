package com.bidpro.service;

import com.bidpro.dao.BidDao;
import com.bidpro.dto.BidDTO;
import com.bidpro.entities.Bid;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BidService {

    @Autowired
    private BidDao bidDao;

    @Transactional
    public BidDTO placeBid(BidDTO bidDTO, String bidderID) {
        Bid bid = new Bid();
        bid.setBidID(UUID.randomUUID().toString());
        bid.setProductID(bidDTO.getProductID());
        bid.setBidderID(bidderID);
        bid.setAmount(bidDTO.getAmount());
        bid.setTimestamp(LocalDateTime.now());
        bid = bidDao.save(bid);
        return new BidDTO(bid);
    }

    public List<Bid> findBidsByProduct(String productID) {
        return bidDao.findByProductProductID(productID);
    }

    public List<Bid> findBidsByBidder(String bidderID) {
        return bidDao.findByBidderUserID(bidderID);
    }

    public List<Bid> findHighestBidByProduct(String productID) {
        return bidDao.findHighestBidByProduct(productID);
    }

    @Transactional
    public BidDTO updateBid(String bidID, BidDTO bidDTO) {
        Bid bid = bidDao.findById(bidID).orElseThrow(() -> new EntityNotFoundException("Bid not found with ID: " + bidID));
        bid.setProductID(bidDTO.getProductID());
        bid.setBidderID(bidDTO.getBidderID());
        bid.setAmount(bidDTO.getAmount());
        bid.setTimestamp(bidDTO.getTimestamp());
        bid = bidDao.save(bid);
        return new BidDTO(bid);
    }
}