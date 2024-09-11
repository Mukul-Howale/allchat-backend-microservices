package com.example.AllChat.service;

import com.example.AllChat.model.User;

public interface UserService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    User findByEmail(String email);
}
