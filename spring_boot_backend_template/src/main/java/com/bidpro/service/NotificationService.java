package com.bidpro.service;

import com.bidpro.dao.NotificationDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.NotificationDTO;
import com.bidpro.entities.Notification;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        User user = userDao.findById(notificationDTO.getUserID())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + notificationDTO.getUserID()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(notificationDTO.getType());
        notification.setContent(notificationDTO.getContent());
        notification.setTimestamp(notificationDTO.getTimestamp() != null ? notificationDTO.getTimestamp() : LocalDateTime.now());
        notification.setRead(notificationDTO.getIsRead() != null ? notificationDTO.getIsRead() : false);
        notification = notificationDao.save(notification);

        notificationDTO.setNotificationID(notification.getNotificationID());
        return notificationDTO;
    }

    public List<NotificationDTO> findNotificationsByUser(String userID) {
        List<Notification> notifications = notificationDao.findByUserUserID(userID);
        return notifications.stream()
                .map(this::toNotificationDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationID(notification.getNotificationID());
        dto.setUserID(notification.getUser().getUserID());
        dto.setType(notification.getType());
        dto.setContent(notification.getContent());
        dto.setTimestamp(notification.getTimestamp());
        dto.setIsRead(notification.isRead());
        return dto;
    }
}