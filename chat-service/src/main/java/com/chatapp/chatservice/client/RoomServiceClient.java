package com.chatapp.chatservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP client that Chat Service uses to validate room existence via Room Service.
 */
@Component
public class RoomServiceClient {

    private final WebClient webClient;

    public RoomServiceClient(@Qualifier("roomWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Synchronous (blocking) check — we must validate before broadcasting.
     * @return true if the room exists in Room Service
     */
    public boolean roomExists(String roomId) {
        try {
            Boolean exists = webClient.get()
                    .uri("/api/v1/rooms/{roomId}/exists", roomId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            System.err.println("[ChatService] Room Service unreachable: " + e.getMessage());
            return false;
        }
    }
}
