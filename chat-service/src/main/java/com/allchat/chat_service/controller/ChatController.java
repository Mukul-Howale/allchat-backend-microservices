package com.allchat.chat_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allchat.chat_service.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @GetMapping("/start")
    public ResponseEntity<String> startChat() throws Exception {
        log.info("Starting chat");
        boolean result = chatService.startChat();
        return result ? new ResponseEntity<>("Chat started", HttpStatus.OK) : new ResponseEntity<>("Unable to start chat", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopChat() throws Exception {
        log.info("Stopping chat");
        boolean result = chatService.stopChat();
        return result ? new ResponseEntity<>("Chat stopped", HttpStatus.OK) : new ResponseEntity<>("Unable to stop chat", HttpStatus. INTERNAL_SERVER_ERROR);
    }
}
