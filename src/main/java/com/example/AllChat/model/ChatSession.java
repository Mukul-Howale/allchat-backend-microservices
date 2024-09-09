package com.example.AllChat.model;

import java.util.List;

public record ChatSession(String sessionId, List<String> participants) {
}
