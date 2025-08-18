package com.bidpro.dao;

import com.bidpro.entities.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRecipientDao extends JpaRepository<MessageRecipient, MessageRecipient.MessageRecipientId> {
    List<MessageRecipient> findByMessageMessageID(Integer messageID);
    List<MessageRecipient> findByUserUserID(String userID);
}