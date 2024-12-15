package com.example.user_service.service.impl;

import com.example.user_service.dto.UserRequestDto;
import com.example.user_service.dto.UserResponseDto;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;

    // Dummy Created
    // Yet to write login for signup
    @Override
    public UserResponseDto signUp(UserRequestDto userRequestDto) {
        return new UserResponseDto();
    }
}
