package com.chatapp.messageservice.payload;

import lombok.Data;

/**
 * Payload used by Chat Service to persist a new message.
 */
@Data
public class SaveMessageRequest {
    private String roomId;
    private String content;
    private String sender;
}
