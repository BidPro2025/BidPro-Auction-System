package com.bidpro.dto;

import com.bidpro.entities.SupportThread;
import java.time.LocalDateTime;
import java.util.List;

public record ConversationDTO(String threadID,String subject,SupportThread.Status status,String ownerID,
        String ownerName,String ownerEmail,LocalDateTime createdAt,LocalDateTime updatedAt,long unreadCount,
        List<Message> messages) {
    public record Message(String messageID,String senderID,String senderName,boolean fromAdmin,String content,LocalDateTime createdAt) {}
    public record Recipient(String userID,String name,String email) {}
}
