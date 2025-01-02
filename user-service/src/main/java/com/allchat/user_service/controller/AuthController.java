package com.allchat.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.allchat.user_service.dto.AuthRequestDto;
import com.allchat.user_service.dto.AuthResponseDto;
import com.allchat.user_service.service.IAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> singUp(@RequestBody AuthRequestDto authRequestDto){
        AuthResponseDto authResponseDto = authService.signUp(authRequestDto);
        return new ResponseEntity<>(authResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/sing-in")
    public ResponseEntity<AuthResponseDto> singnIn(@RequestParam String email, @RequestParam String password){
        AuthResponseDto authResponseDto = authService.signIn(email, password);
        return new ResponseEntity<>(authResponseDto, HttpStatus.OK);
    }
    // log-in
    // log-out

}
