package com.bidpro.service;

import com.bidpro.dao.*;
import com.bidpro.entities.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuctionNotificationService {
    private final ProductDao products;
    private final BidDao bids;
    private final AuctionEndNoticeDao notices;
    private final NotificationService notifications;
    public AuctionNotificationService(ProductDao products, BidDao bids, AuctionEndNoticeDao notices, NotificationService notifications) {
        this.products=products; this.bids=bids; this.notices=notices; this.notifications=notifications;
    }
    public List<String> ready() {
        return products.findEndedNoticeCandidates(List.of(Product.ProductStatus.APPROVED, Product.ProductStatus.SOLD), LocalDateTime.now(), PageRequest.of(0,50));
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public void notifyClosed(String productID) {
        Product product=products.findForUpdateByProductID(productID).orElse(null);
        if (product==null || product.getAuctionEndDate()==null || product.getAuctionEndDate().isAfter(LocalDateTime.now()) ||
            notices.existsById(productID) || !List.of(Product.ProductStatus.APPROVED,Product.ProductStatus.SOLD).contains(product.getProductStatus())) return;
        var winner=bids.findFirstByProductIDOrderByAmountDescTimestampAsc(productID);
        String link="/products/"+productID;
        if (winner.isPresent()) {
            notifications.notifyUser(winner.get().getBidderID(),"AUCTION_WON","You won the auction for "+product.getTitle()+" at INR "+winner.get().getAmount().toPlainString()+". Generate your invoice from My bids.",link);
            notifications.notifyUser(product.getSellerID(),"AUCTION_ENDED","Your auction for "+product.getTitle()+" ended with a winning bid of INR "+winner.get().getAmount().toPlainString()+".","/seller");
        } else notifications.notifyUser(product.getSellerID(),"AUCTION_ENDED","Your auction for "+product.getTitle()+" ended without bids.","/seller");
        AuctionEndNotice notice=new AuctionEndNotice();notice.setProductID(productID);notices.save(notice);
    }
}
