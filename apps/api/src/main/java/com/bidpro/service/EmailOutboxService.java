package com.bidpro.service;

import com.bidpro.dao.EmailOutboxDao;
import com.bidpro.entities.EmailOutbox;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutboxService {
    private final EmailOutboxDao emails;
    private final CurrentUserService current;
    private final ObjectProvider<JavaMailSender> sender;
    private final String mode;
    private final String from;
    public EmailOutboxService(EmailOutboxDao emails, CurrentUserService current, ObjectProvider<JavaMailSender> sender,
            @Value("${app.mail.mode:outbox}") String mode, @Value("${app.mail.from:no-reply@bidpro.local}") String from) {
        if (!List.of("outbox", "smtp").contains(mode)) throw new IllegalArgumentException("MAIL_MODE must be outbox or smtp");
        this.emails = emails; this.current = current; this.sender = sender; this.mode = mode; this.from = from;
    }
    @Transactional
    public void enqueue(String recipient, String subject, String body, boolean sensitive) {
        EmailOutbox mail = new EmailOutbox();
        mail.setRecipient(recipient); mail.setSubject(subject); mail.setBody(body); mail.setSensitive(sensitive);
        mail.setStatus("smtp".equals(mode) ? EmailOutbox.Status.QUEUED : EmailOutbox.Status.LOCAL);
        mail.setNextAttemptAt(LocalDateTime.now());
        emails.save(mail);
    }
    @Transactional(readOnly = true)
    public List<MailView> list() {
        current.requireRoles(User.Role.ADMIN);
        return emails.findTop100ByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }
    public MailView view(EmailOutbox value) {
        return new MailView(value.getId(), value.getRecipient(), value.getSubject(),
            "outbox".equals(mode) ? value.getBody() : "Email preview is available only in local outbox mode.",
            value.getStatus(), value.getCreatedAt(), value.getSentAt(), value.getAttempts(), value.getLastError());
    }
    @Transactional
    public MailView retry(Long id) {
        current.requireRoles(User.Role.ADMIN);
        if (!"smtp".equals(mode)) throw new IllegalArgumentException("SMTP is not configured. Local emails are available in the preview.");
        EmailOutbox mail = emails.findById(id).orElseThrow(() -> new EntityNotFoundException("Email not found"));
        if (mail.getStatus() == EmailOutbox.Status.SENT) throw new IllegalArgumentException("This email was already sent");
        mail.setAttempts(0); mail.setStatus(EmailOutbox.Status.QUEUED); mail.setNextAttemptAt(LocalDateTime.now());
        mail.setLastError(null); return view(emails.save(mail));
    }
    @Transactional
    public void deliver(Long id) {
        EmailOutbox mail = emails.findForUpdate(id).orElse(null);
        if (mail == null || mail.getStatus() != EmailOutbox.Status.QUEUED || mail.getAttempts() >= 5 || !"smtp".equals(mode)
            || (mail.getNextAttemptAt() != null && mail.getNextAttemptAt().isAfter(LocalDateTime.now()))) return;
        mail.setAttempts(mail.getAttempts() + 1);
        try {
            JavaMailSender transport = sender.getIfAvailable();
            if (transport == null) throw new IllegalStateException("Set MAIL_HOST before using SMTP delivery");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from); message.setTo(mail.getRecipient()); message.setSubject(mail.getSubject()); message.setText(mail.getBody());
            transport.send(message);
            mail.setStatus(EmailOutbox.Status.SENT); mail.setSentAt(LocalDateTime.now()); mail.setLastError(null);
            if (mail.isSensitive()) mail.setBody("Password reset instructions were delivered.");
        } catch (Exception error) {
            mail.setLastError("Email delivery failed. Check the SMTP connection and credentials.");
            mail.setNextAttemptAt(LocalDateTime.now().plusMinutes(Math.min(30, mail.getAttempts() * 2L)));
            if (mail.getAttempts() >= 5) mail.setStatus(EmailOutbox.Status.FAILED);
        }
        emails.save(mail);
    }
    public record MailView(Long id, String recipient, String subject, String body, EmailOutbox.Status status,
        LocalDateTime createdAt, LocalDateTime sentAt, int attempts, String lastError) {}
}
