package com.chatapp.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback responses when a downstream service is unavailable.
 * Circuit breaker routes here instead of returning 500/timeout to the client.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/room-service")
    public ResponseEntity<Map<String, String>> roomServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Room Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/message-service")
    public ResponseEntity<Map<String, String>> messageServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Message Service is currently unavailable. Please try again later."));
    }
}
