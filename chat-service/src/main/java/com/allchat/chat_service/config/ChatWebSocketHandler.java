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
 * WebSocket handler for managing real-time chat communications.
 * Extends TextWebSocketHandler to handle text-based WebSocket messages.
 * 
 * This handler manages:
 * - User connections and disconnections
 * - User matching for chat sessions
 * - Message routing between matched users
 * - WebRTC signaling for peer-to-peer connections
 * 
 *  Why use TextWebSocketHandler instead of WebSocketHandler ?
    The WebSocketHandler interface is the base interface for WebSocket handlers.
    The TextWebSocketHandler class extends the AbstractWebSocketHandler
    which implements the WebSocketHandler interface and provides additional methods for handling text messages.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** Maps user IDs to their WebSocket sessions */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    /** Tracks users who are currently looking for a chat match */
    private final Map<String, Boolean> lookingForMatch = new ConcurrentHashMap<>();
    
    /** Maps user IDs to their matched group (set of users in the same chat) */
    private final Map<String, Set<String>> matchedGroups = new ConcurrentHashMap<>();
    
    /** JSON object mapper for message serialization/deserialization */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles new WebSocket connections.
     * Extracts user ID and stores the session for future communication.
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        log.info("WebSocket connection established - User: {}, Session ID: {}, Remote Address: {}, Active Sessions: {}", 
            userId, session.getId(), session.getRemoteAddress(), sessions.keySet());
    }

    /**
     * Processes incoming WebSocket messages.
     * Handles different message types:
     * - looking-for-match: User wants to start a chat
     * - cancel-match: User cancels match search
     * - offer/answer/ice-candidate: WebRTC signaling
     * - chat: Regular chat messages
     * - end-chat: User ends the chat session
     */
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = extractUserId(session);
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        String type = jsonNode.get("type").asText();

        log.info("Received message - Type: {}, From User: {}, Session ID: {}, User's Group: {}", 
            type, userId, session.getId(), matchedGroups.get(userId));
        log.info("Full message payload: {}", message.getPayload());

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
                    log.info("Processing WebRTC message - Type: {}, From: {}, Current Group: {}", 
                        type, userId, matchedGroups.get(userId));
                    forwardWebRTCMessage(message.getPayload(), jsonNode);
                    break;
                case "chat":
                    handleChatMessage(message.getPayload(), userId);
                    break;
                case "end-chat":
                    handleEndChat(userId);
                    break;
                default:
                    log.warn("Unhandled message type received - Type: {}, User: {}", type, userId);
            }
        } catch (Exception e) {
            log.error("Error processing message - Type: {}, User: {}, Error: {}", type, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handles WebSocket connection closures.
     * Cleans up user session and notifies other users in the group.
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        log.info("WebSocket connection closing - User: {}, Session ID: {}, Status: {}, User's Group: {}", 
            userId, session.getId(), closeStatus.getCode(), matchedGroups.get(userId));
        
        try {
            handleUserDisconnection(userId);
            sessions.remove(userId);
            log.info("User disconnected and cleanup completed - User: {}, Remaining Sessions: {}", 
                userId, sessions.keySet());
        } catch (Exception e) {
            log.error("Error during connection cleanup - User: {}, Error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs cleanup when a user disconnects.
     * Removes user from matching pool and handles group cleanup.
     */
    private void handleUserDisconnection(String userId) throws IOException {
        log.debug("Starting user disconnection cleanup - User: {}", userId);
        
        // Remove from looking for match pool
        if (lookingForMatch.remove(userId) != null) {
            log.debug("Removed user from matching pool - User: {}", userId);
        }

        // Handle matched group cleanup
        Set<String> userGroup = matchedGroups.remove(userId);  // Remove user's group entry
        if (userGroup != null) {
            log.info("Removed user from matched groups - User: {}, Group Size: {}", 
                userId, userGroup.size());
            
            // Notify remaining users in the group
            Set<String> remainingUsers = new HashSet<>(userGroup);
            remainingUsers.remove(userId);
            notifyGroupAboutUserLeft(remainingUsers, userId);

            // If group size falls below 2, end the chat for remaining users
            if (remainingUsers.size() < 2) {
                log.info("Group size below minimum threshold, ending group chat - Remaining Size: {}", remainingUsers.size());
                endGroupChat(remainingUsers);
            }
        }
    }

    /**
     * Processes match requests from users.
     * Attempts to pair users looking for matches into chat groups.
     */
    private void handleLookingForMatch(String userId) throws IOException {
        // Log the current state before matching
        log.info("Starting match search - User: {}, Current matched groups: {}, Looking for match: {}", 
            userId, matchedGroups.keySet(), lookingForMatch.keySet());
            
        lookingForMatch.put(userId, true);
        log.info("User started looking for match - User: {}", userId);

        // Try to find matches
        List<String> availableUsers = lookingForMatch.entrySet().stream()
                .filter(entry -> entry.getValue() && !entry.getKey().equals(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Available users for matching - User: {}, Available: {}", userId, availableUsers);

        // If we have enough users for a match (2 or more)
        if (availableUsers.size() >= 1) {
            // Create a new group with the current user and one other user
            Set<String> matchedGroup = new HashSet<>();
            matchedGroup.add(userId);
            matchedGroup.add(availableUsers.get(0));

            log.info("Match found - Creating group: {}", matchedGroup);

            // Update matched groups with separate copies of the set for each user
            for (String uid : matchedGroup) {
                Set<String> userGroup = new HashSet<>(matchedGroup);
                matchedGroups.put(uid, userGroup);
                lookingForMatch.remove(uid);
                log.info("Added user to matched group - User: {}, Group: {}", uid, userGroup);
            }

            // Verify the matching state
            for (String uid : matchedGroup) {
                Set<String> group = matchedGroups.get(uid);
                log.info("Verifying match state - User: {}, Group: {}", uid, group);
            }

            // Notify matched users
            notifyMatchFound(matchedGroup);
            log.info("Match notifications sent - Group: {}", matchedGroup);
        } else {
            log.info("No match found - User: {}, Will keep looking", userId);
        }
    }

    /**
     * Handles match cancellation requests.
     * Removes user from the matching pool.
     */
    private void handleCancelMatch(String userId) throws IOException {
        Boolean wasLooking = lookingForMatch.remove(userId);
        log.info("User cancelled matching - User: {}, Was Looking: {}, Current Looking: {}", 
            userId, wasLooking, lookingForMatch.keySet());
        sendToUser(userId, createMessage("match-cancelled", null, userId));
    }

    /**
     * Routes chat messages between matched users.
     * Only forwards messages to users in the same group.
     */
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

    /**
     * Processes chat end requests.
     * Cleans up the chat group and notifies all participants.
     */
    private void handleEndChat(String userId) throws IOException {
        Set<String> userGroup = matchedGroups.get(userId);
        if (userGroup != null) {
            log.info("Ending chat for user - User: {}, User's Group: {}, All Groups: {}", 
                userId, userGroup, matchedGroups);
            endGroupChat(userGroup);
        } else {
            log.warn("End chat request received from user not in any group - User: {}, All Groups: {}", 
                userId, matchedGroups);
        }
    }

    /**
     * Sends match notifications to users when a match is found.
     * Includes information about all users in the matched group.
     */
    private void notifyMatchFound(Set<String> matchedGroup) throws IOException {
        log.info("Preparing match notifications - Group: {}, Current matched groups: {}", 
            matchedGroup, matchedGroups);
            
        String matchFoundMessage = createMessage("match-found", matchedGroup, null);
        
        for (String userId : matchedGroup) {
            Set<String> userGroup = matchedGroups.get(userId);
            log.info("Sending match notification - User: {}, User's group: {}", userId, userGroup);
            sendToUser(userId, matchFoundMessage);
        }
        
        log.info("Match notifications completed - Group: {}", matchedGroup);
    }

    /**
     * Notifies remaining users when someone leaves the chat group.
     */
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

    /**
     * Ends a group chat session.
     * Cleans up group state and optionally returns users to matching pool.
     */
    private void endGroupChat(Set<String> group) throws IOException {
        if (group == null || group.isEmpty()) {
            log.warn("Attempted to end empty or null group chat - Current Groups: {}", matchedGroups);
            return;
        }

        log.info("Starting group chat end - Group: {}, Current Groups: {}", group, matchedGroups);
        String initiator = group.iterator().next();
        
        // Send end-chat message to all users in the group
        for (String userId : group) {
            // Remove from matched groups
            Set<String> removedGroup = matchedGroups.remove(userId);
            log.info("Removing user from matched groups - User: {}, Removed Group: {}, Remaining Groups: {}", 
                userId, removedGroup, matchedGroups);
            
            // Add back to looking for match except for the initiator
            if (!userId.equals(initiator)) {
                lookingForMatch.put(userId, true);
                log.info("Added user back to matching pool - User: {}, Current Looking: {}", 
                    userId, lookingForMatch.keySet());
            }
            
            // Send end-chat message
            sendToUser(userId, createMessage("end-chat", null, initiator));
        }
        
        log.info("Group chat ended - Final Groups: {}, Looking For Match: {}", 
            matchedGroups, lookingForMatch.keySet());
    }

    /**
     * Handles WebRTC signaling messages between matched users.
     * Ensures messages are only forwarded between properly matched users.
     */
    private void forwardWebRTCMessage(String payload, JsonNode message) throws IOException {
        String to = message.get("to").asText();
        String from = message.get("from").asText();
        String type = message.get("type").asText();

        log.debug("Processing WebRTC message - Type: {}, From: {}, To: {}", type, from, to);
        
        // Debug logging for matched groups
        Set<String> fromGroup = matchedGroups.get(from);
        Set<String> toGroup = matchedGroups.get(to);
        log.info("Matched groups state - From user group: {}, To user group: {}", 
            fromGroup != null ? fromGroup : "null", 
            toGroup != null ? toGroup : "null");

        // Verify both users are properly matched
        boolean isValidMatch = fromGroup != null && toGroup != null && 
                             fromGroup.contains(to) && toGroup.contains(from) &&
                             fromGroup.equals(toGroup);

        if (isValidMatch) {
            WebSocketSession recipientSession = sessions.get(to);
            if (recipientSession != null && recipientSession.isOpen()) {
                log.info("Forwarding WebRTC signal - Type: {}, From: {}, To: {}", type, from, to);
                recipientSession.sendMessage(new TextMessage(payload));
            } else {
                log.warn("Cannot forward WebRTC signal - Recipient session invalid or closed - To: {}", to);
            }
        } else {
            log.warn("WebRTC signal forwarding blocked - Invalid match state - From: {}, To: {}, FromGroup: {}, ToGroup: {}", 
                    from, to, fromGroup, toGroup);
        }
    }

    /**
     * Creates a formatted JSON message for WebSocket communication.
     * @param type Message type identifier
     * @param users Set of users involved (optional)
     * @param userId Specific user ID (optional)
     * @return JSON string representation of the message
     */
    private String createMessage(String type, Set<String> users, String userId) throws IOException {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", type);
        if (users != null) message.put("users", objectMapper.valueToTree(users));
        if (userId != null) message.put("userId", userId);

        String messageStr = objectMapper.writeValueAsString(message);
        log.debug("Created message - Type: {}, Size: {} bytes", type, messageStr.length());
        return messageStr;
    }

    /**
     * Sends a message to a specific user if their session is active.
     */
    private void sendToUser(String userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
            log.debug("Message sent to user - User: {}, Session ID: {}", userId, session.getId());
        } else {
            log.warn("Failed to send message - User session invalid or closed - User: {}", userId);
        }
    }

    /**
     * Extracts user ID from WebSocket session query parameters.
     * Expected format: "?userId=<value>"
     */
    private String extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        String userId = query.substring(query.indexOf("=") + 1);
        log.debug("Extracted user ID from session - User: {}, Session ID: {}", userId, session.getId());
        return userId;
    }
}
