package com.bidpro.dao;

import com.bidpro.entities.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SupportMessageDao extends JpaRepository<SupportMessage,String> {
    List<SupportMessage> findByThreadIDOrderByCreatedAtAscMessageIDAsc(String threadID);
    long countByThreadIDAndCreatedAtAfterAndSenderIDNot(String threadID,LocalDateTime date,String senderID);
}
