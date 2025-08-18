package com.bidpro.dao;

import com.bidpro.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageDao extends JpaRepository<Message, Integer> {
	@Query("SELECT m FROM Message m WHERE m.sender.adminId = :adminId")
    List<Message> findBySenderId(@Param("adminId") String adminId);

    @Query("SELECT m FROM Message m WHERE m.timestamp BETWEEN :startDate AND :endDate")
    List<Message> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    List<Message> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Message> findByChannelChannelID(Integer channelID);
    @Query("SELECT m FROM Message m WHERE m.sender.adminId = :adminId")
    List<Message> findBySenderAdminId(String adminId);
    
}