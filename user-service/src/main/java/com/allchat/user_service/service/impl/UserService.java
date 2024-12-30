package com.allchat.user_service.service.impl;

import com.allchat.user_service.dto.UserRequestDto;
import com.allchat.user_service.dto.UserResponseDto;
import com.allchat.user_service.repository.UserRepository;
import com.allchat.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;

}
