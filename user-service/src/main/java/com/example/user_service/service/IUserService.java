package com.example.user_service.service;

import com.example.user_service.dto.UserRequestDto;
import com.example.user_service.dto.UserResponseDto;

public interface IUserService {

    UserResponseDto signUp(UserRequestDto userRequestDto);
}
