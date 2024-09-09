package com.example.AllChat.service;

import java.io.IOException;

public interface WebSocketMessageSender {
    void sendMessage(String userId, String message) throws IOException;
}
