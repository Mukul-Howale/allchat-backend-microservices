package com.example.AllChat.service.impl;

import com.example.AllChat.dto.AuthResponse;
import com.example.AllChat.dto.LoginRequest;
import com.example.AllChat.dto.SignupRequest;
import com.example.AllChat.model.User;
import com.example.AllChat.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private JwtUtil jwtUtil;
    private static boolean existsByUsername(String username) {
        return true;
    }

    private static boolean existsByEmail(String email) {
        return true;
    }

    private static void save(User user) {
    }

    private User findByEmail(String email) {
        return new User();
    }

    public ResponseEntity<Object> signup(SignupRequest signupRequest) {
        if (existsByUsername(signupRequest.getUsername())) return ResponseEntity.badRequest().body("Username is already taken");
        if (existsByEmail(signupRequest.getEmail())) return ResponseEntity.badRequest().body("Email is already in use");
        User user = new User(signupRequest.getName(), signupRequest.getUsername(),signupRequest.getEmail(), signupRequest.getPassword());
        save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(token);
    }

    public ResponseEntity<Object> login(LoginRequest loginRequest){
        User user = findByEmail(loginRequest.getEmail());
        if (user == null || !user.getPassword().equals(loginRequest.getPassword())) return ResponseEntity.badRequest().body("Invalid email or password");
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(token);
    }
}
