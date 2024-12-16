package com.allchat.user_service.service;

import com.allchat.user_service.dto.UserRequestDto;
import com.allchat.user_service.dto.UserResponseDto;


public interface IUserService {

    UserResponseDto signUp(UserRequestDto userRequestDto);
}
