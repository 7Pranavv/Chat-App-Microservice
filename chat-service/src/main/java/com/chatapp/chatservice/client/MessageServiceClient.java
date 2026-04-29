package com.chatapp.chatservice.client;

import com.chatapp.chatservice.payload.ChatMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * HTTP client that Chat Service uses to persist messages in Message Service.
 */
@Component
public class MessageServiceClient {

    private final WebClient webClient;

    public MessageServiceClient(@Qualifier("messageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fire-and-forget persistence: broadcast first, then save asynchronously.
     * Errors are logged but do NOT fail the WebSocket response.
     */
    public void saveMessage(ChatMessage chatMessage) {
        Map<String, String> body = Map.of(
                "roomId",  chatMessage.getRoomId(),
                "content", chatMessage.getContent(),
                "sender",  chatMessage.getSender()
        );

        webClient.post()
                .uri("/api/v1/messages")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        resp -> { /* success — no-op */ },
                        err  -> System.err.println("[ChatService] Failed to persist message: " + err.getMessage())
                );
    }
}
