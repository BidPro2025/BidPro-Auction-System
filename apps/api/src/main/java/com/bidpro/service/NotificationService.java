package com.bidpro.service;

import com.bidpro.dao.NotificationDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.NotificationDTO;
import com.bidpro.entities.Notification;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class NotificationService {
    private final NotificationDao notifications;
    private final UserDao users;
    private final CurrentUserService current;
    private final EmailOutboxService emails;
    public NotificationService(NotificationDao notifications, UserDao users, CurrentUserService current, EmailOutboxService emails) {
        this.notifications = notifications; this.users = users; this.current = current; this.emails = emails;
    }
    public NotificationDTO notifyUser(String userID, String type, String content, String link) {
        var user = users.findById(userID).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Notification notification = new Notification(); notification.setUser(user); notification.setType(type);
        notification.setContent(content.length() > 255 ? content.substring(0,255) : content);
        notification.setLink(link); notification.setTimestamp(LocalDateTime.now());
        notification = notifications.save(notification);
        emails.enqueue(user.getEmail(), "BidPro: " + type.replace('_',' ').toLowerCase(), notification.getContent(), false);
        return toDTO(notification);
    }
    public NotificationDTO createNotification(NotificationDTO input) {
        current.requireRoles(com.bidpro.entities.User.Role.ADMIN);
        return notifyUser(input.getUserID(), input.getType(), input.getContent(), input.getLink());
    }
    @Transactional(readOnly = true)
    public List<NotificationDTO> own() { return findNotificationsByUser(current.id()); }
    @Transactional(readOnly = true)
    public List<NotificationDTO> findNotificationsByUser(String userID) {
        if (!current.isAdmin() && !current.id().equals(userID)) throw new AccessDeniedException("You can only access your own notifications");
        return notifications.findTop100ByUserUserIDOrderByTimestampDesc(userID).stream().map(this::toDTO).toList();
    }
    public NotificationDTO markRead(Integer id) {
        Notification notification = notifications.findById(id).orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!notification.getUser().getUserID().equals(current.id())) throw new AccessDeniedException("You can only update your own notifications");
        notification.setRead(true); notification.setUpdatedAt(LocalDateTime.now()); return toDTO(notifications.save(notification));
    }
    public void markAllRead() { notifications.markAllRead(current.id(), LocalDateTime.now()); }
    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO(); dto.setNotificationID(notification.getNotificationID());
        dto.setUserID(notification.getUser().getUserID()); dto.setType(notification.getType()); dto.setContent(notification.getContent());
        dto.setTimestamp(notification.getTimestamp()); dto.setIsRead(notification.isRead()); dto.setLink(notification.getLink()); return dto;
    }
}
