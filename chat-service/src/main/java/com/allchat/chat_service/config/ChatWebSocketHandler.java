package com.allchat.chat_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info(session.getId());
        log.info("afterConnectionEstablished");
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        // check for messages like offer or answer
        // based on that send the message to the other peer
        log.info("handleMessage");
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        sessions.remove(session.getId());
        log.info("afterConnectionClosed");
    }
}
