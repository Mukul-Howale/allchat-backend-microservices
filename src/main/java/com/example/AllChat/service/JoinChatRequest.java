package com.example.AllChat.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinChatRequest {
    private String userId;
    private int groupSize;
}
