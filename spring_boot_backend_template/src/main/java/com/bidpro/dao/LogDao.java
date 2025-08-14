package com.bidpro.dao;

import com.bidpro.entities.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogDao extends JpaRepository<Log, Integer> {
    List<Log> findByEntityType(String entityType);
    List<Log> findByEntityID(String entityID);
    List<Log> findByPerformedBy(String performedBy);

    @Query("SELECT l FROM Log l WHERE l.logTimestamp BETWEEN :startDate AND :endDate")
    List<Log> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                             @Param("endDate") LocalDateTime endDate);
    
    List<Log> findByEntityTypeAndEntityID(String entityType, String entityID);
}