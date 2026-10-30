package com.bidpro.controller;

import com.bidpro.dto.*;
import com.bidpro.entities.SupportThread;
import com.bidpro.service.ConversationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService service){this.service=service;}
    @GetMapping public ApiResponse<List<ConversationDTO>> list(){return ApiResponse.success(service.list(),"Conversations retrieved");}
    @GetMapping("/recipients") public ApiResponse<List<ConversationDTO.Recipient>> recipients(){return ApiResponse.success(service.recipients(),"Recipients retrieved");}
    @PostMapping public ApiResponse<ConversationDTO> create(@Valid @RequestBody CreateRequest input){
        return ApiResponse.success(service.create(input.subject(),input.message(),input.recipientUserID()),"Conversation started");
    }
    @GetMapping("/{id}") public ApiResponse<ConversationDTO> one(@PathVariable String id){return ApiResponse.success(service.get(id),"Conversation retrieved");}
    @PostMapping("/{id}/messages") public ApiResponse<ConversationDTO> reply(@PathVariable String id,@Valid @RequestBody ReplyRequest input){return ApiResponse.success(service.reply(id,input.content()),"Message sent");}
    @PutMapping("/{id}/status") public ApiResponse<ConversationDTO> status(@PathVariable String id,@Valid @RequestBody StatusRequest input){return ApiResponse.success(service.status(id,input.status()),"Conversation status updated");}
    public record CreateRequest(@NotBlank @Size(max=120) String subject,@NotBlank @Size(max=4000) String message,@Size(max=50) String recipientUserID) {}
    public record ReplyRequest(@NotBlank @Size(max=4000) String content) {}
    public record StatusRequest(@NotNull SupportThread.Status status) {}
}
