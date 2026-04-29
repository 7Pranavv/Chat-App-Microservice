package com.chatapp.messageservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Message is now a first-class MongoDB document (own collection).
 * In the monolith it was embedded inside Room — here it lives independently,
 * linked to a room via roomId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @Indexed                    // fast lookup by roomId
    private String roomId;

    private String content;

    private String sender;

    private LocalDateTime timeStamp;
}
