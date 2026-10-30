package com.bidpro.service;

import com.bidpro.dao.MessageDao;
import com.bidpro.dao.AdminDao;
import com.bidpro.dao.ChannelDao;
import com.bidpro.dto.MessageDTO;
import com.bidpro.entities.Message;
import com.bidpro.entities.Admin;
import com.bidpro.entities.Channel;
import com.bidpro.util.BidProUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling message-related operations.
 * Provides methods to send messages, retrieve messages by ID, channel, or date range.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageDao messageDao;
    private final AdminDao adminDao;
    private final ChannelDao channelDao;
    private final ModelMapper mapper;
    private final BidProUtils utils;

    /**
     * Sends a new message and saves it to the database.
     *
     * @param dto      The MessageDTO containing message details
     * @param senderId The ID of the admin sending the message
     * @return The saved MessageDTO
     * @throws EntityNotFoundException if the sender or channel is not found
     */
    @Transactional
    public MessageDTO sendMessage(MessageDTO dto, String senderId) {
        Admin sender = adminDao.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
        Channel channel = channelDao.findById(dto.getChannelID())
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        Message msg = mapper.map(dto, Message.class);
        msg.setSender(sender);
        msg.setTimestamp(LocalDateTime.now());
        msg.setChannel(channel);
        msg = messageDao.save(msg);
        return mapper.map(msg, MessageDTO.class);
    }

    /**
     * Retrieves a message by its ID.
     *
     * @param id The ID of the message
     * @return The corresponding MessageDTO
     * @throws EntityNotFoundException if the message is not found
     */
    public MessageDTO findMessageById(Integer id) {
        return mapper.map(messageDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found")),
                MessageDTO.class);
    }

    /**
     * Retrieves messages within a specified date range.
     *
     * @param startDate The start date of the range
     * @param endDate   The end date of the range
     * @return A list of messages within the date range
     * @throws IllegalArgumentException if the date range is invalid
     */
    public List<Message> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return messageDao.findByTimestampBetween(startDate, endDate);
    }

    /**
     * Retrieves messages by channel ID.
     *
     * @param channelID The ID of the channel
     * @return A list of messages associated with the channel
     * @throws EntityNotFoundException if the channel is not found
     */
    public List<Message> findByChannelChannelID(Integer channelID) {
        channelDao.findById(channelID)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
        return messageDao.findByChannelChannelID(channelID);
    }
}