package com.bidpro.service;

import com.bidpro.dao.ActivityDao;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.Activity;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public void logActivity(String userID, String action, String details) {
        User user = userDao.findById(userID)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userID));
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setAction(action);
        activity.setTimestamp(LocalDateTime.now());
        activity.setDetails(details);
        activityDao.save(activity);
    }

    public List<Activity> findActivitiesByUser(String userID) {
        return activityDao.findByUserUserID(userID);
    }

    public List<Activity> findActivitiesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return activityDao.findByDateRange(startDate, endDate);
    }
}