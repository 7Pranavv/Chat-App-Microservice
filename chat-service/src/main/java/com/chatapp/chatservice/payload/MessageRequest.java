package com.chatapp.chatservice.payload;

import lombok.Data;

/** Incoming STOMP message from frontend client. */
@Data
public class MessageRequest {
    private String roomId;
    private String content;
    private String sender;
}
