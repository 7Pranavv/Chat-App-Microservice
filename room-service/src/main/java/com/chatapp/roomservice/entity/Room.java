package com.chatapp.roomservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a chat room.
 * Messages are stored separately in Message Service — only roomId is shared.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;          // MongoDB _id

    private String roomId;      // Human-readable room identifier (e.g. "team-alpha")
}
