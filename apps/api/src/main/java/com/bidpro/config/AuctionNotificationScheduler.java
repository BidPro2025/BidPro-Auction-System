package com.bidpro.config;
import com.bidpro.service.AuctionNotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration @EnableScheduling
@ConditionalOnProperty(name="app.notifications.scheduler-enabled",havingValue="true",matchIfMissing=true)
public class AuctionNotificationScheduler {
    private static final Logger log=LoggerFactory.getLogger(AuctionNotificationScheduler.class);
    private final AuctionNotificationService notifications;
    public AuctionNotificationScheduler(AuctionNotificationService notifications) {this.notifications=notifications;}
    @Scheduled(fixedDelayString="${app.notifications.poll-ms:30000}",initialDelayString="${app.notifications.initial-delay-ms:10000}")
    public void closedAuctions() {
        for(String id:notifications.ready()) {
            try {notifications.notifyClosed(id);}
            catch(RuntimeException failure) {log.error("Auction end notification failed for product {}",id,failure);}
        }
    }
}
