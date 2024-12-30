package com.allchat.user_service.controller;

import com.allchat.user_service.dto.UserResponseDto;
import com.allchat.user_service.dto.UserRequestDto;
import com.allchat.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

}
