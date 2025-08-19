package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.MessageDTO;
import com.bidpro.entities.Message;
import com.bidpro.service.MessageService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/{messageID}")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessageById(@PathVariable Integer messageID) {
        MessageDTO dto = messageService.findMessageById(messageID); // Directly assign MessageDTO
        return ResponseEntity.ok(ApiResponse.success(dto, "Message retrieved successfully"));
    }

    @GetMapping("/channel/{channelID}")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessagesByChannel(@PathVariable Integer channelID) {
        List<Message> messages = messageService.findByChannelChannelID(channelID);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(messageDTOs, "Messages retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(@Valid @RequestBody MessageDTO messageDTO, Principal principal) {
        String senderID = principal.getName();
        MessageDTO sentMessage = messageService.sendMessage(messageDTO, senderID);
        return ResponseEntity.ok(ApiResponse.success(sentMessage, "Message sent successfully"));
    }
}