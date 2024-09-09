package com.example.AllChat.controller;

import com.example.AllChat.service.ChatService;
import com.example.AllChat.service.JoinChatRequest;
import com.example.AllChat.service.LeaveChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/join")
    public ResponseEntity<String> joinChat(@RequestBody JoinChatRequest request) {
        String sessionId = chatService.joinChatQueue(request.getUserId(), request.getGroupSize());
        return ResponseEntity.ok(sessionId);
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveChat(@RequestBody LeaveChatRequest request) {
        chatService.leaveChat(request.getUserId());
        return ResponseEntity.ok().build();
    }
}
