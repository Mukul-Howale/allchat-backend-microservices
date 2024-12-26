package com.allchat.chat_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import io.micrometer.common.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String , WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("afterConnectionEstablished");
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
        log.info("handleMessage");
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.info("handleTransportError");
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        log.info("afterConnectionClosed");
    }

    @Override
    public boolean supportsPartialMessages() {
        log.info("supportsPartialMessages");
        return false;
    }
}
