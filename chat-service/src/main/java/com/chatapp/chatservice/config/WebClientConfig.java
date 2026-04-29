package com.chatapp.chatservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.room-service.url}")
    private String roomServiceUrl;

    @Value("${services.message-service.url}")
    private String messageServiceUrl;

    @Bean("roomWebClient")
    public WebClient roomWebClient() {
        return WebClient.builder().baseUrl(roomServiceUrl).build();
    }

    @Bean("messageWebClient")
    public WebClient messageWebClient() {
        return WebClient.builder().baseUrl(messageServiceUrl).build();
    }
}
