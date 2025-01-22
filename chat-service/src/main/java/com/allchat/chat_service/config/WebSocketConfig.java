package com.allchat.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket Configuration class that sets up and configures WebSocket endpoints for the chat service.
 * This class enables WebSocket support and defines the WebSocket connection handling.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * Handler for WebSocket messages and connection lifecycle events
     */
    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * Interceptor for WebSocket handshake process to handle authentication and session setup
     */
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;

    /**
     * Constructor to inject required dependencies
     * @param chatWebSocketHandler Handles WebSocket communication
     * @param chatHandshakeInterceptor Manages WebSocket connection establishment
     */
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, ChatHandshakeInterceptor chatHandshakeInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.chatHandshakeInterceptor = chatHandshakeInterceptor;
    }

    /**
     * Configures WebSocket endpoints and their properties
     * @param registry WebSocketHandlerRegistry to register handlers and customize WebSocket endpoints
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws")  // Registers WebSocket endpoint at /ws
                .setAllowedOrigins("*")                   // Enables CORS for all origins
                .addInterceptors(chatHandshakeInterceptor)// Adds handshake interceptor for connection setup
                .setHandshakeHandler(new DefaultHandshakeHandler());
    }
}
