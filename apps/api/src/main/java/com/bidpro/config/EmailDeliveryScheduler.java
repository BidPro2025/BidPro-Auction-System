package com.bidpro.config;

import com.bidpro.dao.EmailOutboxDao;
import com.bidpro.service.EmailOutboxService;
import java.time.LocalDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import com.bidpro.entities.EmailOutbox;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration @EnableScheduling
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
public class EmailDeliveryScheduler {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EmailDeliveryScheduler.class);
    private final EmailOutboxDao emails;
    private final EmailOutboxService delivery;
    public EmailDeliveryScheduler(EmailOutboxDao emails, EmailOutboxService delivery) { this.emails = emails; this.delivery = delivery; }
    @Scheduled(fixedDelayString = "${app.mail.poll-ms:30000}", initialDelayString = "${app.mail.initial-delay-ms:1000}")
    public void deliverQueuedEmails() {
        for (var mail : emails.findTop25ByStatusAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByIdAsc(EmailOutbox.Status.QUEUED, 5, LocalDateTime.now())) {
            try { delivery.deliver(mail.getId()); }
            catch (RuntimeException failure) {
                // Do not expose the recipient, SMTP settings, reset body or transport error.
                LOG.warn("Email delivery transaction failed for outbox entry {}; next poll can retry", mail.getId());
            }
        }
    }
}
