package com.example.AllChat.service.impl;

import com.example.AllChat.model.ChatSession;
import com.example.AllChat.service.WebSocketMessageSender;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ChatService {
    private final Map<Integer, Queue<String>> waitingUsers = new ConcurrentHashMap<>();
    private final Map<String, ChatSession> activeSessions = new ConcurrentHashMap<>();

    private final WebSocketMessageSender messageSender;

    public ChatService(WebSocketMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public String joinChatQueue(String userId, int groupSize) {
        Queue<String> queue = waitingUsers.computeIfAbsent(groupSize, k -> new ConcurrentLinkedQueue<>());
        queue.offer(userId);

        if (queue.size() >= groupSize) {
            List<String> participants = new ArrayList<>();
            for (int i = 0; i < groupSize; i++) {
                participants.add(queue.poll());
            }
            String sessionId = createChatSession(participants);
            notifyUsersOfMatch(participants, sessionId);
            return sessionId;
        }
        return null;
    }

    public void leaveChat(String userId) {
        ChatSession session = findSessionByUserId(userId);
        if (session != null) {
            activeSessions.remove(session.sessionId());
            notifyUsersOfPartnerLeft(session.participants(), userId);
        }
    }

    public void handleWebRTCSignaling(String userId, String message) {
        ChatSession session = findSessionByUserId(userId);
        if (session != null) {
            session.participants().stream()
                    .filter(participantId -> !participantId.equals(userId))
                    .forEach(participantId -> {
                        try {
                            messageSender.sendMessage(participantId, message);
                        } catch (IOException e) {
                            // Handle exception
                        }
                    });
        }
    }

    public void handleUserDisconnection(String userId) {
        leaveChat(userId);
    }

    private String createChatSession(List<String> participants) {
        String sessionId = generateSessionId();
        ChatSession session = new ChatSession(sessionId, participants);
        activeSessions.put(sessionId, session);
        return sessionId;
    }

    private void notifyUsersOfMatch(List<String> participants, String sessionId) {
        String message = String.format("{\"type\":\"match\",\"sessionId\":\"%s\"}", sessionId);
        participants.forEach(userId -> {
            try {
                messageSender.sendMessage(userId, message);
            } catch (IOException e) {
                // Handle exception
            }
        });
    }

    private void notifyUsersOfPartnerLeft(List<String> participants, String leftUserId) {
        String message = String.format("{\"type\":\"partnerLeft\",\"userId\":\"%s\"}", leftUserId);
        participants.stream()
                .filter(userId -> !userId.equals(leftUserId))
                .forEach(userId -> {
                    try {
                        messageSender.sendMessage(userId, message);
                    } catch (IOException e) {
                        // Handle exception
                    }
                });
    }

    private ChatSession findSessionByUserId(String userId) {
        return activeSessions.values().stream()
                .filter(session -> session.participants().contains(userId))
                .findFirst()
                .orElse(null);
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
