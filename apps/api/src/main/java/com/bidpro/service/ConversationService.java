package com.bidpro.service;

import com.bidpro.dao.*;
import com.bidpro.dto.ConversationDTO;
import com.bidpro.entities.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class ConversationService {
    private final SupportThreadDao threads;
    private final SupportMessageDao messages;
    private final UserDao users;
    private final CurrentUserService current;
    private final NotificationService notifications;
    public ConversationService(SupportThreadDao threads,SupportMessageDao messages,UserDao users,
            CurrentUserService current,NotificationService notifications) {
        this.threads=threads;this.messages=messages;this.users=users;this.current=current;this.notifications=notifications;
    }
    public List<ConversationDTO> list() {
        current.requireApproved();
        return (current.isAdmin()?threads.findAllByOrderByUpdatedAtDesc():threads.findByOwnerIDOrderByUpdatedAtDesc(current.id()))
            .stream().map(thread->toDTO(thread,false)).toList();
    }
    public List<ConversationDTO.Recipient> recipients() {
        current.requireRoles(User.Role.ADMIN);
        return users.findAll().stream().filter(u->u.getStatus()==User.Status.APPROVED && u.getRole()!=User.Role.ADMIN)
            .map(u->new ConversationDTO.Recipient(u.getUserID(),name(u),u.getEmail())).toList();
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ConversationDTO create(String subject,String content,String recipientID) {
        current.requireApproved();
        User owner=current.user();
        if(current.isAdmin()) {
            if(recipientID==null || recipientID.isBlank()) throw new IllegalArgumentException("Choose an account for this conversation");
            owner=users.findById(recipientID).orElseThrow(()->new EntityNotFoundException("Recipient not found"));
            if(owner.getStatus()!=User.Status.APPROVED || owner.getRole()==User.Role.ADMIN) throw new IllegalArgumentException("Choose an approved seller or bidder account");
        } else if(recipientID!=null && !recipientID.isBlank()) {
            throw new AccessDeniedException("Messages can only be sent to marketplace support");
        }
        var thread=new SupportThread();thread.setThreadID(UUID.randomUUID().toString());thread.setOwnerID(owner.getUserID());
        thread.setCreatedBy(current.id());thread.setSubject(subject.trim());
        if(current.isAdmin()) thread.setAdminReadAt(LocalDateTime.now());else thread.setOwnerReadAt(LocalDateTime.now());
        threads.saveAndFlush(thread);append(thread,content);return toDTO(thread,true);
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ConversationDTO get(String id) {
        var thread=locked(id);requireAccess(thread);
        if(current.isAdmin())thread.setAdminReadAt(LocalDateTime.now());else thread.setOwnerReadAt(LocalDateTime.now());
        threads.save(thread);return toDTO(thread,true);
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ConversationDTO reply(String id,String content) {
        var thread=locked(id);requireAccess(thread);
        if(thread.getStatus()!=SupportThread.Status.OPEN)throw new IllegalArgumentException("Reopen this conversation before replying");
        append(thread,content);return toDTO(thread,true);
    }
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ConversationDTO status(String id,SupportThread.Status status) {
        var thread=locked(id);requireAccess(thread);thread.setStatus(status);thread.setUpdatedAt(LocalDateTime.now());
        return toDTO(threads.save(thread),true);
    }
    private SupportThread locked(String id) {
        current.requireApproved();return threads.findForUpdate(id).orElseThrow(()->new EntityNotFoundException("Conversation not found"));
    }
    private void requireAccess(SupportThread thread) {
        current.requireApproved();if(!current.isAdmin() && !thread.getOwnerID().equals(current.id()))
            throw new AccessDeniedException("This conversation is private to its participant and marketplace support");
    }
    private void append(SupportThread thread,String content) {
        var message=new SupportMessage();message.setMessageID(UUID.randomUUID().toString());message.setThreadID(thread.getThreadID());
        message.setSenderID(current.id());message.setContent(content.trim());messages.saveAndFlush(message);
        thread.setUpdatedAt(message.getCreatedAt());
        if(current.isAdmin())thread.setAdminReadAt(message.getCreatedAt());else thread.setOwnerReadAt(message.getCreatedAt());
        threads.save(thread);
        String notice="New message: "+thread.getSubject();String link="/messages/"+thread.getThreadID();
        if(current.isAdmin())notifications.notifyUser(thread.getOwnerID(),"MESSAGE",notice,link);
        else users.findByRole(User.Role.ADMIN).stream().filter(u->u.getStatus()==User.Status.APPROVED)
            .forEach(admin->notifications.notifyUser(admin.getUserID(),"MESSAGE",notice,link));
    }
    private ConversationDTO toDTO(SupportThread thread,boolean includeMessages) {
        var owner=users.findById(thread.getOwnerID()).orElseThrow(()->new EntityNotFoundException("Conversation account not found"));
        var readAt=current.isAdmin()?thread.getAdminReadAt():thread.getOwnerReadAt();
        long unread=messages.countByThreadIDAndCreatedAtAfterAndSenderIDNot(thread.getThreadID(),readAt==null?LocalDateTime.of(1970,1,1,0,0):readAt,current.id());
        var contents=includeMessages?messages.findByThreadIDOrderByCreatedAtAscMessageIDAsc(thread.getThreadID()).stream().map(message->{
            var sender=users.findById(message.getSenderID()).orElseThrow(()->new EntityNotFoundException("Sender not found"));
            return new ConversationDTO.Message(message.getMessageID(),sender.getUserID(),name(sender),sender.getRole()==User.Role.ADMIN,message.getContent(),message.getCreatedAt());
        }).toList():List.<ConversationDTO.Message>of();
        return new ConversationDTO(thread.getThreadID(),thread.getSubject(),thread.getStatus(),thread.getOwnerID(),name(owner),
            current.isAdmin()?owner.getEmail():null,thread.getCreatedAt(),thread.getUpdatedAt(),unread,contents);
    }
    private String name(User user){return user.getFirstName()+" "+user.getLastName();}
}
