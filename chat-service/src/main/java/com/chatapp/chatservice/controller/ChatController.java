package com.chatapp.chatservice.controller;

import com.chatapp.chatservice.client.MessageServiceClient;
import com.chatapp.chatservice.client.RoomServiceClient;
import com.chatapp.chatservice.payload.ChatMessage;
import com.chatapp.chatservice.payload.MessageRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

/**
 * Chat Service WebSocket Controller
 *
 * Flow:
 *   1. Frontend sends STOMP message to /app/sendMessage/{roomId}
 *   2. Validate room exists (calls Room Service)
 *   3. Broadcast ChatMessage to /topic/room/{roomId} (all subscribers get it)
 *   4. Async-persist via Message Service (fire-and-forget)
 *
 * Port: 8083
 */
@Controller
public class ChatController {

    private final RoomServiceClient roomServiceClient;
    private final MessageServiceClient messageServiceClient;

    public ChatController(RoomServiceClient roomServiceClient,
                          MessageServiceClient messageServiceClient) {
        this.roomServiceClient = roomServiceClient;
        this.messageServiceClient = messageServiceClient;
    }

    @MessageMapping("/sendMessage/{roomId}")       // client sends to:   /app/sendMessage/{roomId}
    @SendTo("/topic/room/{roomId}")               // subscribers listen: /topic/room/{roomId}
    public ChatMessage sendMessage(
            @DestinationVariable String roomId,
            @Payload MessageRequest request
    ) {
        // 1. Validate room via Room Service
        if (!roomServiceClient.roomExists(roomId)) {
            throw new RuntimeException("Room not found: " + roomId);
        }

        // 2. Build broadcast message
        ChatMessage chatMessage = new ChatMessage(
                request.getContent(),
                request.getSender(),
                roomId,
                LocalDateTime.now()
        );

        // 3. Persist asynchronously via Message Service (does not block broadcast)
        messageServiceClient.saveMessage(chatMessage);

        // 4. Return — Spring broadcasts this to /topic/room/{roomId}
        return chatMessage;
    }
}
