package com.example.AllChat.controller;

import com.example.AllChat.dto.AuthResponse;
import com.example.AllChat.dto.LoginRequest;
import com.example.AllChat.dto.SignupRequest;
import com.example.AllChat.model.User;
import com.example.AllChat.service.impl.UserService;
import com.example.AllChat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        ResponseEntity<?> response = userService.signup(signupRequest);
        return getResponse(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        ResponseEntity<?> response = userService.login(loginRequest);
        return getResponse(response);
    }

    private static ResponseEntity<?> getResponse(ResponseEntity<?> response){
        if (response.getStatusCode().is4xxClientError()) return ResponseEntity.badRequest().body((String) response.getBody());
        else if(response.getStatusCode().isSameCodeAs(HttpStatus.OK)) return ResponseEntity.ok(new AuthResponse((String) response.getBody()));
        return ResponseEntity.internalServerError().body("Unexpected response type");
    }
}
