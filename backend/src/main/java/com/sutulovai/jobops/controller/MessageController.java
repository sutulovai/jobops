package com.sutulovai.jobops.controller;

import com.sutulovai.jobops.dto.request.GenerateMessageRequest;
import com.sutulovai.jobops.dto.request.LogCopiedMessageRequest;
import com.sutulovai.jobops.dto.response.OutreachMessageResponse;
import com.sutulovai.jobops.service.message.MessageGenerationService;
import com.sutulovai.jobops.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages")
public class MessageController {

    private final MessageGenerationService messageGenerationService;

    public MessageController(MessageGenerationService messageGenerationService) {
        this.messageGenerationService = messageGenerationService;
    }

    @GetMapping
    public ResponseEntity<List<OutreachMessageResponse>> list() {
        return ResponseEntity.ok(messageGenerationService.listMessages(SecurityUtils.currentUserId()));
    }

    @PostMapping("/generate")
    public ResponseEntity<OutreachMessageResponse> generate(@Valid @RequestBody GenerateMessageRequest request) {
        return ResponseEntity.ok(messageGenerationService.generateMessage(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/log-copy")
    public ResponseEntity<OutreachMessageResponse> logCopied(@Valid @RequestBody LogCopiedMessageRequest req) {
        return ResponseEntity.ok(messageGenerationService.logCopiedMessage(SecurityUtils.currentUserId(), req));
    }

    @PostMapping("/{id}/copied")
    public ResponseEntity<OutreachMessageResponse> markCopied(@PathVariable UUID id) {
        return ResponseEntity.ok(messageGenerationService.markCopied(id, SecurityUtils.currentUserId()));
    }

    @PostMapping("/{id}/sent")
    public ResponseEntity<OutreachMessageResponse> markSent(@PathVariable UUID id) {
        return ResponseEntity.ok(messageGenerationService.markSent(id, SecurityUtils.currentUserId()));
    }
}
