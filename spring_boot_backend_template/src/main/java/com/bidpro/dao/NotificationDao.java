package com.bidpro.dao;

import com.bidpro.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDao extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUserID(String userID);
}