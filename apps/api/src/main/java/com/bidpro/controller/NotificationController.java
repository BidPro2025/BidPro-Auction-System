package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }
    @GetMapping public ApiResponse<List<NotificationDTO>> own() { return ApiResponse.success(notifications.own(), "Your notifications"); }
    @PutMapping("/{id}/read") public ApiResponse<NotificationDTO> read(@PathVariable Integer id) { return ApiResponse.success(notifications.markRead(id), "Notification marked read"); }
    @PutMapping("/read-all") public ApiResponse<Void> allRead() { notifications.markAllRead(); return ApiResponse.success(null, "All notifications marked read"); }
}
