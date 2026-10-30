package com.bidpro.controller;
import com.bidpro.dto.ApiResponse;
import com.bidpro.service.EmailOutboxService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admins/email-outbox")
public class EmailOutboxController {
    private final EmailOutboxService mail;
    public EmailOutboxController(EmailOutboxService mail) { this.mail = mail; }
    @GetMapping public ApiResponse<List<EmailOutboxService.MailView>> list() { return ApiResponse.success(mail.list(), "Email delivery records"); }
    @PostMapping("/{id}/retry") public ApiResponse<EmailOutboxService.MailView> retry(@PathVariable Long id) { return ApiResponse.success(mail.retry(id), "Email queued for another delivery attempt"); }
}
