package com.bidpro.service;

import com.bidpro.dao.MessageRecipientDao;
import com.bidpro.dao.MessageDao;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.MessageRecipientDTO;
import com.bidpro.entities.Message;
import com.bidpro.entities.MessageRecipient;
import com.bidpro.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageRecipientService {

    @Autowired
    private MessageRecipientDao messageRecipientDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public MessageRecipientDTO addRecipient(MessageRecipientDTO recipientDTO) {
        // Validate message existence
        Message message = messageDao.findById(recipientDTO.getMessageID())
                .orElseThrow(() -> new EntityNotFoundException("Message not found with ID: " + recipientDTO.getMessageID()));

        // Validate user existence
        User user = userDao.findById(recipientDTO.getUserID())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + recipientDTO.getUserID()));

        // Check if recipient already exists for this message and user
        MessageRecipient.MessageRecipientId id = new MessageRecipient.MessageRecipientId(recipientDTO.getMessageID(), recipientDTO.getUserID());
        if (messageRecipientDao.existsById(id)) {
            throw new IllegalArgumentException("Recipient already exists for message ID: " + recipientDTO.getMessageID() + " and user ID: " + recipientDTO.getUserID());
        }

        // Create and save MessageRecipient entity
        MessageRecipient recipient = new MessageRecipient();
        recipient.setId(id); // Corrected from setld to setId
        recipient.setMessage(message);
        recipient.setUser(user);
        recipient.setCreatedAt(LocalDateTime.now());
        recipient.setUpdatedAt(LocalDateTime.now());
        messageRecipientDao.save(recipient);

        return recipientDTO;
    }

    public List<MessageRecipient> findRecipientsByMessage(Integer messageID) {
        return messageRecipientDao.findByMessageMessageID(messageID);
    }

    public List<MessageRecipient> findRecipientsByUser(String userID) {
        return messageRecipientDao.findByUserUserID(userID);
    }
}