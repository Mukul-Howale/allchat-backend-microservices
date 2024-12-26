package com.allchat.chat_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allchat.chat_service.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/start")
    public ResponseEntity<String> startChat() throws Exception {
        boolean result = chatService.startChat();
        return result ? new ResponseEntity<>("Chat started", HttpStatus.OK) : new ResponseEntity<>("Unable to start chat", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopChat() throws Exception {
        boolean result = chatService.stopChat();
        return result ? new ResponseEntity<>("Chat stopped", HttpStatus.OK) : new ResponseEntity<>("Unable to stop chat", HttpStatus. INTERNAL_SERVER_ERROR);
    }
}
