package com.bidpro.config;

import com.bidpro.service.AuctionSettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.auctions.settlement-enabled", havingValue = "true", matchIfMissing = true)
public class AuctionSettlementScheduler {
    private static final Logger log = LoggerFactory.getLogger(AuctionSettlementScheduler.class);
    private final AuctionSettlementService settlements;

    public AuctionSettlementScheduler(AuctionSettlementService settlements) {
        this.settlements = settlements;
    }

    @Scheduled(fixedDelayString = "${app.auctions.settlement-poll-ms:30000}",
            initialDelayString = "${app.auctions.settlement-initial-delay-ms:10000}")
    public void settleClosedAuctions() {
        for (String productID : settlements.ready()) {
            try {
                settlements.settle(productID);
            } catch (RuntimeException failure) {
                // Leave this auction eligible for retry without delaying other winners.
                log.error("Automatic auction settlement failed for product {}", productID, failure);
            }
        }
    }
}
