package com.bidpro;

import com.bidpro.config.AuctionNotificationScheduler;
import com.bidpro.config.AuctionSettlementScheduler;
import com.bidpro.service.AuctionNotificationService;
import com.bidpro.service.AuctionSettlementService;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuctionSchedulerIsolationTest {
    @Test
    void settlementFailureDoesNotSkipOtherWinners() {
        AuctionSettlementService service = mock(AuctionSettlementService.class);
        when(service.ready()).thenReturn(List.of("broken", "healthy"));
        doThrow(new IllegalStateException("simulated failure")).when(service).settle("broken");
        new AuctionSettlementScheduler(service).settleClosedAuctions();
        verify(service).settle("healthy");
    }

    @Test
    void notificationFailureDoesNotSkipOtherClosedAuctions() {
        AuctionNotificationService service = mock(AuctionNotificationService.class);
        when(service.ready()).thenReturn(List.of("broken", "healthy"));
        doThrow(new IllegalStateException("simulated failure")).when(service).notifyClosed("broken");
        new AuctionNotificationScheduler(service).closedAuctions();
        verify(service).notifyClosed("healthy");
    }
}
