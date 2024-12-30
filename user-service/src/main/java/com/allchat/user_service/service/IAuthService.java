package com.allchat.user_service.service;

import com.allchat.user_service.dto.AuthRequestDto;
import com.allchat.user_service.dto.AuthResponseDto;

public interface IAuthService {

    AuthResponseDto signUp(AuthRequestDto authRequestDto);
}
