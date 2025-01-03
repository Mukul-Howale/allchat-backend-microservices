package com.allchat.chat_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
    Why use TextWebSocketHandler instead of WebSocketHandler ?
    The WebSocketHandler interface is the base interface for WebSocket handlers.
    The TextWebSocketHandler class extends the AbstractWebSocketHandler
    which implements the WebSocketHandler interface and provides additional methods for handling text messages.
 */

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String , WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> activeUsers = new ConcurrentHashMap<>(); // Track active users

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        
        // Send existing active users to the new user
        for (Map.Entry<String, Boolean> entry : activeUsers.entrySet()) {
            if (entry.getValue() && !entry.getKey().equals(userId)) {
                String message = new ObjectMapper().writeValueAsString(Map.of(
                    "type", "userJoined",
                    "userId", entry.getKey()
                ));
                session.sendMessage(new TextMessage(message));
            }
        }
        
        log.info("User connected: {}", userId);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);
        
        String type = jsonNode.get("type").asText();
        String userId = extractUserId(session);
        
        log.info("Received message type: {} from: {}", type, userId);

        switch (type) {
            case "ready":
                // When user is ready to chat, mark them as active and broadcast to others
                activeUsers.put(userId, true);
                broadcastUserJoined(userId);
                break;
            case "offer":
            case "answer":
            case "ice-candidate":
                String to = jsonNode.get("to").asText();
                if (sessions.containsKey(to)) {
                    sessions.get(to).sendMessage(message);
                }
                break;
            case "chat":
                // Broadcast chat messages to all active users
                for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                    if (activeUsers.get(entry.getKey()) && entry.getValue().isOpen() 
                        && !userId.equals(entry.getKey())) {
                        entry.getValue().sendMessage(message);
                    }
                }
                break;
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        sessions.remove(userId);
        activeUsers.remove(userId);
        
        broadcastUserLeft(userId);
        log.info("User disconnected: {}", userId);
    }

    private String extractUserId(WebSocketSession session){
        String query = session.getUri().getQuery();
        return query.substring(query.indexOf("=") + 1);
    }

    private void broadcastUserJoined(String userId) throws Exception {
        String message = new ObjectMapper().writeValueAsString(Map.of(
            "type", "userJoined",
            "userId", userId
        ));
        
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    private void broadcastUserLeft(String userId) throws Exception{
        String message = new ObjectMapper().writeValueAsString(Map.of(
            "type", "userLeft",
            "userId", userId
        ));
        
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }
}
