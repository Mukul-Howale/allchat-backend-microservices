package com.example.AllChat.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    // Getter and setter
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

}
