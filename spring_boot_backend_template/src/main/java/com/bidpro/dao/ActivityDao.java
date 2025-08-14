package com.bidpro.dao;

import com.bidpro.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityDao extends JpaRepository<Activity, Integer> {
    List<Activity> findByUserUserID(String userID);

    @Query("SELECT a FROM Activity a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    List<Activity> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
}