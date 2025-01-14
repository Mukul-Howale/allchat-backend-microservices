package com.allchat.chat_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.micrometer.common.lang.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
    Why use TextWebSocketHandler instead of WebSocketHandler ?
    The WebSocketHandler interface is the base interface for WebSocket handlers.
    The TextWebSocketHandler class extends the AbstractWebSocketHandler
    which implements the WebSocketHandler interface and provides additional methods for handling text messages.
 */

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lookingForMatch = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> matchedGroups = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        log.info("User connected: {}", userId);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = extractUserId(session);
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        String type = jsonNode.get("type").asText();

        log.info("Received message type: {} from user: {}", type, userId);

        switch (type) {
            case "looking-for-match":
                handleLookingForMatch(userId);
                break;
            case "cancel-match":
                handleCancelMatch(userId);
                break;
            case "offer":
            case "answer":
            case "ice-candidate":
                forwardWebRTCMessage(message.getPayload(), jsonNode);
                break;
            case "chat":
                handleChatMessage(message.getPayload(), userId);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        handleUserDisconnection(userId);
        sessions.remove(userId);
        log.info("User disconnected: {}", userId);
    }

    private void handleUserDisconnection(String userId) throws IOException {
        // Remove from looking for match pool
        lookingForMatch.remove(userId);

        // Handle matched group cleanup
        Set<String> userGroup = matchedGroups.get(userId);
        if (userGroup != null) {
            userGroup.remove(userId);
            
            // Notify remaining users in the group
            notifyGroupAboutUserLeft(userGroup, userId);

            // If group size falls below 2, end the chat
            if (userGroup.size() < 2) {
                endGroupChat(userGroup);
            }
        }
    }

    private void handleLookingForMatch(String userId) throws IOException {
        lookingForMatch.put(userId, true);
        log.info("User {} is looking for a match", userId);

        // Try to find matches
        List<String> availableUsers = lookingForMatch.entrySet().stream()
                .filter(entry -> entry.getValue() && !entry.getKey().equals(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // If we have enough users for a match (2 or more)
        if (availableUsers.size() >= 1) {
            // Create a new group with the current user and one other user
            Set<String> matchedGroup = new HashSet<>();
            matchedGroup.add(userId);
            matchedGroup.add(availableUsers.get(0));

            // Update matched groups
            matchedGroup.forEach(uid -> {
                matchedGroups.put(uid, matchedGroup);
                lookingForMatch.remove(uid);
            });

            // Notify matched users
            notifyMatchFound(matchedGroup);
        }
    }

    private void handleCancelMatch(String userId) throws IOException {
        lookingForMatch.remove(userId);
        sendToUser(userId, createMessage("match-cancelled", null, userId));
        log.info("User {} cancelled matching", userId);
    }

    private void handleChatMessage(String payload, String fromUserId) throws IOException {
        Set<String> userGroup = matchedGroups.get(fromUserId);
        if (userGroup != null) {
            for (String toUserId : userGroup) {
                if (!toUserId.equals(fromUserId)) {
                    sendToUser(toUserId, payload);
                }
            }
        }
    }

    private void notifyMatchFound(Set<String> matchedGroup) throws IOException {
        String matchFoundMessage = createMessage("match-found", matchedGroup, null);
        for (String userId : matchedGroup) {
            sendToUser(userId, matchFoundMessage);
        }
    }

    private void notifyGroupAboutUserLeft(Set<String> group, String leftUserId) throws IOException {
        String message = createMessage("user-left-match", null, leftUserId);
        for (String userId : group) {
            if (!userId.equals(leftUserId)) {
                sendToUser(userId, message);
            }
        }
    }

    private void endGroupChat(Set<String> group) throws IOException {
        String message = createMessage("chat-ended", null, null);
        for (String userId : group) {
            sendToUser(userId, message);
            matchedGroups.remove(userId);
        }
    }

    private void forwardWebRTCMessage(String payload, JsonNode message) throws IOException {
        String to = message.get("to").asText();
        String from = message.get("from").asText();
        String type = message.get("type").asText();

        // Only forward if users are in the same matched group
        Set<String> fromGroup = matchedGroups.get(from);
        if (fromGroup != null && fromGroup.contains(to)) {
            WebSocketSession recipientSession = sessions.get(to);
            if (recipientSession != null && recipientSession.isOpen()) {
                log.info("Forwarding {} signal from {} to {}", type, from, to);
                recipientSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    private String createMessage(String type, Set<String> users, String userId) throws IOException {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", type);
        if (users != null) {
            message.put("users", objectMapper.valueToTree(users));
        }
        if (userId != null) {
            message.put("userId", userId);
        }
        return objectMapper.writeValueAsString(message);
    }

    private void sendToUser(String userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        return query.substring(query.indexOf("=") + 1);
    }
}
