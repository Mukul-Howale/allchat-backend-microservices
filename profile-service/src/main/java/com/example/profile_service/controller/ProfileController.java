package com.example.profile_service.controller;

import com.example.profile_service.dto.ProfileResponse;
import com.example.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(String userId){
        return profileService.createProfile(userId);
    }

    @GetMapping
    @ResponseStatus()
}
