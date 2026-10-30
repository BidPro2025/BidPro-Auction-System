package com.bidpro.dao;

import com.bidpro.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDao extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUserID(String userID);
    List<Notification> findTop100ByUserUserIDOrderByTimestampDesc(String userID);
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Notification n set n.read = true, n.updatedAt = :now where n.user.userID = :id and n.read = false")
    void markAllRead(@org.springframework.data.repository.query.Param("id") String id, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}