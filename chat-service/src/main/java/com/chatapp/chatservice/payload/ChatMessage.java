package com.chatapp.chatservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The object broadcast to all subscribers on /topic/room/{roomId}.
 * Mirrors the Message entity structure so the frontend stays unchanged.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String content;
    private String sender;
    private String roomId;
    private LocalDateTime timeStamp;
}
