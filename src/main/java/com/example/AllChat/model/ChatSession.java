package com.example.AllChat.model;

import lombok.Getter;

import java.util.List;

@Getter
public record ChatSession(String sessionId, List<String> participants) {
}
