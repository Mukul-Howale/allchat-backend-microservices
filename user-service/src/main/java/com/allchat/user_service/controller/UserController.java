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

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> singUp(@RequestBody UserRequestDto userRequestDto){
        UserResponseDto userResponseDto = userService.signUp(userRequestDto);
        return new ResponseEntity<>(userResponseDto, HttpStatus.CREATED);
    }

    // log-in
    // log-out
}
