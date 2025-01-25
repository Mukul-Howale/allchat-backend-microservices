package com.allchat.profile_service.controller;

import com.allchat.profile_service.dto.ProfileResponseDto;
import com.allchat.profile_service.service.IProfileService;
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    @PostMapping("/create-profile")
    public ResponseEntity<ProfileResponseDto> createProfile(String userId) throws Exception {
        return new ResponseEntity<>(profileService.createProfile(userId), HttpStatus.CREATED);
    }

    @GetMapping("get-profile")
    public ResponseEntity<ProfileResponseDto> getProfile(String profileId) throws Exception {
        return new ResponseEntity<>(profileService.getProfile(profileId), HttpStatus.OK);
    }

    @PatchMapping("like/{profileId}")
    public ResponseEntity<Boolean> addLike(@PathVariable String profileId) throws Exception {
        boolean likeAdded = profileService.addLike(profileId);
        if(likeAdded) return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/dislike/{profileId}")
    public ResponseEntity<Boolean> addDislike(@PathVariable String profileId) throws Exception{
        boolean disLikeAdded = profileService.addDislike(profileId);
        if(disLikeAdded) return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
