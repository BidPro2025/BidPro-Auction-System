package com.bidpro;

import com.bidpro.config.EmailDeliveryScheduler;
import com.bidpro.dao.EmailOutboxDao;
import com.bidpro.entities.EmailOutbox;
import com.bidpro.service.EmailOutboxService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class EmailSchedulerIsolationTest {
    @Test void transactionFailureDoesNotBlockLaterQueuedMessages() {
        var dao = mock(EmailOutboxDao.class); var delivery = mock(EmailOutboxService.class);
        var first = new EmailOutbox(); first.setId(1L); var second = new EmailOutbox(); second.setId(2L);
        when(dao.findTop25ByStatusAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByIdAsc(
            eq(EmailOutbox.Status.QUEUED), eq(5), any(LocalDateTime.class))).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("transaction unavailable")).when(delivery).deliver(1L);
        new EmailDeliveryScheduler(dao, delivery).deliverQueuedEmails();
        verify(delivery).deliver(2L);
    }
}
