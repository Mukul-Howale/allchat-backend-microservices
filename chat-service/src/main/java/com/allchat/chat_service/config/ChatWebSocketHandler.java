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
        log.info("WebSocket connection established - User: {}, Session ID: {}, Remote Address: {}",
        userId, session.getId(), session.getRemoteAddress());
        log.info("Current active sessions count: {}", sessions.size());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = extractUserId(session);
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        String type = jsonNode.get("type").asText();

        log.info("Received message - Type: {}, From User: {}, Session ID: {}, Payload Size: {} bytes", 
        type, userId, session.getId(), message.getPayloadLength());
        log.info("Message payload: {}", message.getPayload());

        try {
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
                default:
                    log.warn("Unhandled message type received - Type: {}, User: {}", type, userId);
            }
        } catch (Exception e) {
            log.error("Error processing message - Type: {}, User: {}, Error: {}", type, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        log.info("WebSocket connection closing - User: {}, Session ID: {}, Status: {}, Reason: {}", 
            userId, session.getId(), closeStatus.getCode(), closeStatus.getReason());
        
        try {
            handleUserDisconnection(userId);
            sessions.remove(userId);
            log.info("User disconnected and cleanup completed - User: {}", userId);
            log.debug("Remaining active sessions: {}", sessions.size());
        } catch (Exception e) {
            log.error("Error during connection cleanup - User: {}, Error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private void handleUserDisconnection(String userId) throws IOException {
        log.debug("Starting user disconnection cleanup - User: {}", userId);
        
        // Remove from looking for match pool
        if (lookingForMatch.remove(userId) != null) {
            log.debug("Removed user from matching pool - User: {}", userId);
        }

        // Handle matched group cleanup
        Set<String> userGroup = matchedGroups.get(userId);
        if (userGroup != null) {
            int originalSize = userGroup.size();
            userGroup.remove(userId);
            log.info("Removed user from matched group - User: {}, Original Group Size: {}, New Size: {}", 
                userId, originalSize, userGroup.size());
            
            // Notify remaining users in the group
            notifyGroupAboutUserLeft(userGroup, userId);

            // If group size falls below 2, end the chat
            if (userGroup.size() < 2) {
                log.info("Group size below minimum threshold, ending group chat - Remaining Size: {}", userGroup.size());
                endGroupChat(userGroup);
            }
        }
    }

    private void handleLookingForMatch(String userId) throws IOException {
        lookingForMatch.put(userId, true);
        log.info("User started looking for match - User: {}", userId);
        log.info("Current users looking for match: {}", lookingForMatch.size());

        // Try to find matches
        List<String> availableUsers = lookingForMatch.entrySet().stream()
                .filter(entry -> entry.getValue() && !entry.getKey().equals(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.debug("Available users for matching: {}", availableUsers.size());

        // If we have enough users for a match (2 or more)
        if (availableUsers.size() >= 1) {
            // Create a new group with the current user and one other user
            Set<String> matchedGroup = new HashSet<>();
            matchedGroup.add(userId);
            matchedGroup.add(availableUsers.get(0));

            log.info("Match found - Users: {}", matchedGroup);

            // Update matched groups
            matchedGroup.forEach(uid -> {
                matchedGroups.put(uid, matchedGroup);
                lookingForMatch.remove(uid);
            });

            // Notify matched users
            notifyMatchFound(matchedGroup);
            log.debug("Match notifications sent - Group Size: {}", matchedGroup.size());
        }
    }

    private void handleCancelMatch(String userId) throws IOException {
        Boolean wasLooking = lookingForMatch.remove(userId);
        log.info("User cancelled matching - User: {}, Was Looking: {}", userId, wasLooking);
        sendToUser(userId, createMessage("match-cancelled", null, userId));
        log.debug("Current users still looking for match: {}", lookingForMatch.size());
    }

    private void handleChatMessage(String payload, String fromUserId) throws IOException {
        Set<String> userGroup = matchedGroups.get(fromUserId);
        if (userGroup != null) {
            log.debug("Processing chat message - From: {}, Group Size: {}", fromUserId, userGroup.size());
            int messagesSent = 0;
            
            for (String toUserId : userGroup) {
                if (!toUserId.equals(fromUserId)) {
                    sendToUser(toUserId, payload);
                    messagesSent++;
                }
            }
            
            log.info("Chat message forwarded - From: {}, Recipients: {}", fromUserId, messagesSent);
        } else {
            log.warn("Chat message received from user not in any group - User: {}", fromUserId);
        }
    }

    private void notifyMatchFound(Set<String> matchedGroup) throws IOException {
        String matchFoundMessage = createMessage("match-found", matchedGroup, null);
        log.info("Sending match found notifications - Group Size: {}", matchedGroup.size());
        
        for (String userId : matchedGroup) {
            sendToUser(userId, matchFoundMessage);
        }
        
        log.debug("Match notifications sent to all users in group");
    }

    private void notifyGroupAboutUserLeft(Set<String> group, String leftUserId) throws IOException {
        String message = createMessage("user-left-match", null, leftUserId);
        log.info("Notifying group about user departure - Left User: {}, Remaining Group Size: {}", 
            leftUserId, group.size());
        
        int notificationsSent = 0;
        for (String userId : group) {
            if (!userId.equals(leftUserId)) {
                sendToUser(userId, message);
                notificationsSent++;
            }
        }
        
        log.debug("User left notifications sent - Recipients: {}", notificationsSent);
    }

    private void endGroupChat(Set<String> group) throws IOException {
        String message = createMessage("chat-ended", null, null);
        log.info("Ending group chat - Group Size: {}", group.size());
        
        for (String userId : group) {
            sendToUser(userId, message);
            matchedGroups.remove(userId);
        }
        
        log.debug("Group chat ended - Cleanup completed");
    }

    private void forwardWebRTCMessage(String payload, JsonNode message) throws IOException {
        String to = message.get("to").asText();
        String from = message.get("from").asText();
        String type = message.get("type").asText();

        log.debug("Processing WebRTC message - Type: {}, From: {}, To: {}", type, from, to);

        // Only forward if users are in the same matched group
        Set<String> fromGroup = matchedGroups.get(from);
        if (fromGroup != null && fromGroup.contains(to)) {
            WebSocketSession recipientSession = sessions.get(to);
            if (recipientSession != null && recipientSession.isOpen()) {
                log.info("Forwarding WebRTC signal - Type: {}, From: {}, To: {}", type, from, to);
                recipientSession.sendMessage(new TextMessage(payload));
            } else {
                log.warn("Cannot forward WebRTC signal - Recipient session invalid or closed - To: {}", to);
            }
        } else {
            log.warn("WebRTC signal forwarding blocked - Users not in same group - From: {}, To: {}", from, to);
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
        
        String messageStr = objectMapper.writeValueAsString(message);
        log.debug("Created message - Type: {}, Size: {} bytes", type, messageStr.length());
        return messageStr;
    }

    private void sendToUser(String userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
            log.debug("Message sent to user - User: {}, Session ID: {}", userId, session.getId());
        } else {
            log.warn("Failed to send message - User session invalid or closed - User: {}", userId);
        }
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        String userId = query.substring(query.indexOf("=") + 1);
        log.debug("Extracted user ID from session - User: {}, Session ID: {}", userId, session.getId());
        return userId;
    }
}
