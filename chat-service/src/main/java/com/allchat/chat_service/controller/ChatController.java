package com.allchat.chat_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping("/start")
    public ResponseEntity<Boolean> startChat() throws Exception {
        return new ResponseEntity<>(chatService.startChat(), HttpStatus.OK);
    }

    @RequestMapping("/stop")
    public ResponseEntity<Boolean> stopChat() throws Exception {
        return new ResponseEntity<>(chatService.stopChat(), HttpStatus.OK);
    }
}
