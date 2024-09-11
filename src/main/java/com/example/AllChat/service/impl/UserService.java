package com.example.AllChat.service.impl;

import com.example.AllChat.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public boolean existsByUsername(String username) {
        return true;
    }

    public boolean existsByEmail(String email) {
        return true
    }

    public void save(User user) {
    }

    public User findByEmail(String email) {
        return new User();
    }
}
