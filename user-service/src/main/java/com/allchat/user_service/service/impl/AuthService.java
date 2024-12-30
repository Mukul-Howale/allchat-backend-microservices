package com.allchat.user_service.service.impl;

import com.allchat.user_service.dto.AuthRequestDto;
import com.allchat.user_service.dto.AuthResponseDto;
import com.allchat.user_service.service.IAuthService;

public class AuthService implements IAuthService {

    // Dummy Created
    // Yet to write login for signup
    @Override
    public AuthResponseDto signUp(AuthRequestDto authRequestDto) {
        return new AuthResponseDto();
    }
}
