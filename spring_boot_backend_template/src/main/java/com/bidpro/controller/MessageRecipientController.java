package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.MessageRecipientDTO;
import com.bidpro.service.MessageRecipientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message-recipients")
public class MessageRecipientController {

    @Autowired
    private MessageRecipientService messageRecipientService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageRecipientDTO>> addRecipient(@Valid @RequestBody MessageRecipientDTO recipientDTO) {
        MessageRecipientDTO addedRecipient = messageRecipientService.addRecipient(recipientDTO);
        return ResponseEntity.ok(ApiResponse.success(addedRecipient, "Recipient added successfully"));
    }

    @GetMapping("/message/{messageID}")
    public ResponseEntity<ApiResponse<List<MessageRecipientDTO>>> getRecipientsByMessage(@PathVariable Integer messageID) {
        List<MessageRecipientDTO> recipients = messageRecipientService.findRecipientsByMessage(messageID).stream()
                .map(recipient -> {
                    MessageRecipientDTO dto = new MessageRecipientDTO();
                    dto.setMessageID(recipient.getMessageID());
                    dto.setUserID(recipient.getUserID());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(recipients, "Recipients retrieved successfully"));
    }
}