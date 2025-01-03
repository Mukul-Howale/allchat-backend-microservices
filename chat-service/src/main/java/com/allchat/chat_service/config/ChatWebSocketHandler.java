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

import java.io.IOException;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        log.info("User connected: {}", userId);

        // Send existing active users to the new user
        for (Map.Entry<String, Boolean> entry : activeUsers.entrySet()) {
            if (entry.getValue() && !entry.getKey().equals(userId)) {
                notifyUserJoined(session, entry.getKey());
            }
        }
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = extractUserId(session);
        JsonNode jsonNode = objectMapper.readTree(message.getPayload().toString());
        String type = jsonNode.get("type").asText();

        log.info("Received message type: {} from user: {}", type, userId);

        switch (type) {
            case "ready":
                handleReadyMessage(userId);
                break;
            case "offer":
            case "answer":
            case "ice-candidate":
                forwardWebRTCMessage(message.getPayload().toString(), jsonNode);
                break;
            case "chat":
                broadcastChatMessage(message.getPayload().toString(), userId);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        sessions.remove(userId);
        activeUsers.remove(userId);

        // Notify others that user has left
        String message = objectMapper.writeValueAsString(Map.of(
            "type", "userLeft",
            "userId", userId
        ));

        for (WebSocketSession otherSession : sessions.values()) {
            if (otherSession.isOpen()) {
                otherSession.sendMessage(new TextMessage(message));
            }
        }

        log.info("User disconnected: {}", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = extractUserId(session);
        log.error("Transport error for user {}: {}", userId, exception.getMessage());
        // Clean up the user's session
        sessions.remove(userId);
        activeUsers.remove(userId);
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

    private void handleReadyMessage(String userId) throws IOException {
        // Mark user as active
        activeUsers.put(userId, true);
        log.info("User ready: {}", userId);

        // Notify all other users about this user
        for (WebSocketSession session : sessions.values()) {
            String otherUserId = extractUserId(session);
            if (!otherUserId.equals(userId)) {
                notifyUserJoined(session, userId);
            }
        }
    }

    private void forwardWebRTCMessage(String payload, JsonNode message) throws IOException {
        String to = message.get("to").asText();
        String from = message.get("from").asText();
        String type = message.get("type").asText();

        WebSocketSession recipientSession = sessions.get(to);
        if (recipientSession != null && recipientSession.isOpen()) {
            log.info("Forwarding {} signal from {} to {}", type, from, to);
            recipientSession.sendMessage(new TextMessage(payload));
        } else {
            log.warn("Cannot forward {} signal to user {}: user not found or session closed", type, to);
        }
    }

    private void notifyUserJoined(WebSocketSession session, String joinedUserId) throws IOException {
        String message = objectMapper.writeValueAsString(Map.of(
            "type", "userJoined",
            "userId", joinedUserId
        ));
        session.sendMessage(new TextMessage(message));
    }

    private void broadcastChatMessage(String payload, String fromUserId) throws IOException {
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (!entry.getKey().equals(fromUserId) && 
                activeUsers.getOrDefault(entry.getKey(), false) && 
                entry.getValue().isOpen()) {
                entry.getValue().sendMessage(new TextMessage(payload));
            }
        }
    }
}
