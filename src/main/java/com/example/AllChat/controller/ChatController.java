package com.example.AllChat.controller;

import com.example.AllChat.dto.JoinChatRequest;
import com.example.AllChat.dto.LeaveChatRequest;
import com.example.AllChat.service.impl.ChatService;

import com.example.AllChat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/join")
    public ResponseEntity<String> joinChat(@RequestBody JoinChatRequest request, @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                String sessionId = chatService.joinChatQueue(jwtUtil.getUsernameFromToken(token), request.getGroupSize());
                return ResponseEntity.ok(sessionId);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveChat(@RequestBody LeaveChatRequest request, @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                chatService.leaveChat(username);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
