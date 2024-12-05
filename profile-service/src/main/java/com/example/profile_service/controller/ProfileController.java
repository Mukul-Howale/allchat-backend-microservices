package com.example.profile_service.controller;

import com.example.profile_service.dto.ProfileResponseDto;
import com.example.profile_service.service.impl.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/create-profile")
    public ResponseEntity<ProfileResponseDto> createProfile(String userId) throws Exception {
        return new ResponseEntity<>(profileService.createProfile(userId), HttpStatus.CREATED);
    }

    @GetMapping("get-profile")
    public ResponseEntity<ProfileResponseDto> getProfile(String profileId) throws Exception {
        return new ResponseEntity<>(profileService.getProfile(profileId), HttpStatus.OK);
    }


}
