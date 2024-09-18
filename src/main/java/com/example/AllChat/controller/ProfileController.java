package com.example.AllChat.controller;

import com.example.AllChat.dto.AuthResponse;
import com.example.AllChat.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")

public class ProfileController {

    @Autowired
    UserService userService;

    @PostMapping("/save")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        ResponseEntity<?> response = userService.getProfile(userId);
        return getResponse(response);
    }

    private static ResponseEntity<?> getResponse(ResponseEntity<?> response){
        if (response.getStatusCode().is4xxClientError()) return ResponseEntity.badRequest().body((String) response.getBody());
        else if(response.getStatusCode().isSameCodeAs(HttpStatus.OK)) return ResponseEntity.ok(new AuthResponse((String) response.getBody()));
        return ResponseEntity.internalServerError().body("Unexpected response type");
    }
}
