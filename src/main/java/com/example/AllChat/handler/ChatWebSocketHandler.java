package com.example.AllChat.handler;

import com.example.AllChat.service.impl.ChatService;
import com.example.AllChat.service.impl.WebSocketMessageSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.lang.NonNull;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final WebSocketMessageSenderImpl messageSender;

    public ChatWebSocketHandler(ChatService chatService, WebSocketMessageSenderImpl messageSender) {
        this.chatService = chatService;
        this.messageSender = messageSender;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        messageSender.addSession(userId, session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = extractUserId(session);
        chatService.handleWebRTCSignaling(userId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = extractUserId(session);
        messageSender.removeSession(userId);
        chatService.handleUserDisconnection(userId);
    }

    private String extractUserId(@NonNull WebSocketSession session) {
        // Extract user ID from session attributes or JWT token
        return "user123"; // Placeholder - implement actual extraction logic
    }
}
