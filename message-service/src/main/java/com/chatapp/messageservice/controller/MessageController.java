package com.chatapp.messageservice.controller;

import com.chatapp.messageservice.entity.Message;
import com.chatapp.messageservice.payload.SaveMessageRequest;
import com.chatapp.messageservice.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message Service REST Controller
 *
 * Responsibilities:
 *   POST /api/v1/messages              — persist a message (called internally by Chat Service)
 *   GET  /api/v1/messages/{roomId}     — paginated history for a room
 *
 * Port: 8082
 */
@RestController
@RequestMapping("/api/v1/messages")

public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // ─── Save Message (internal — called by Chat Service after broadcast) ─────
    @PostMapping
    public ResponseEntity<Message> saveMessage(@RequestBody SaveMessageRequest request) {
        Message message = new Message();
        message.setRoomId(request.getRoomId());
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─── Get Paginated Messages ───────────────────────────────────────────────
    @GetMapping("/{roomId}")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("timeStamp").ascending());
        Page<Message> messagePage = messageRepository.findByRoomId(roomId, pageable);
        return ResponseEntity.ok(messagePage.getContent());
    }
}
