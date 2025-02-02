package com.allchat.profile_service.controller;

import com.allchat.profile_service.dto.ProfileResponseDto;
import com.allchat.profile_service.dto.ReportDto;
import com.allchat.profile_service.service.IProfileService;
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

    @PostMapping("/create-profile/{userId}")
    public ResponseEntity<ProfileResponseDto> createProfile(@PathVariable String userId) throws Exception {
        return new ResponseEntity<>(profileService.createProfile(userId), HttpStatus.CREATED);
    }

    @GetMapping("/get-profile/{profileId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable String profileId) throws Exception {
        return new ResponseEntity<>(profileService.getProfile(profileId), HttpStatus.OK);
    }

    @PatchMapping("/like/{profileId}")
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

    @PatchMapping("/send-friend-request/{fromId}/{toId}")
    public ResponseEntity<Boolean> sendFriendRequest(@PathVariable String fromId, @PathVariable String toId) throws Exception{
        boolean requestSent = profileService.sendFriendRequest(fromId, toId);
        if(requestSent) return new ResponseEntity<>(true, HttpStatus.OK);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/accept-friend-request/{fromId}/{toId}")
    public ResponseEntity<Boolean> acceptFriendRequest(@PathVariable String fromId, @PathVariable String toId) throws Exception{
        boolean requestAccepted = profileService.acceptFriendRequest(fromId, toId);
        if(requestAccepted) return new ResponseEntity<>(true, HttpStatus.OK);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/remove-friend/{profileId}/{friendId}")
    public ResponseEntity<Boolean> removeFriend(@PathVariable String profileId, @PathVariable String friendId) throws Exception{
        boolean friendRemoved = profileService.removeFriend(profileId,friendId);
        if(friendRemoved) return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/get-friends/{profileId}")
    public ResponseEntity<List<String>> getFriendsId(@PathVariable String profileId,
                                                   @RequestParam Integer offset,
                                                   @RequestParam Integer limit) throws Exception{
        List<String> profileIds = profileService.getFriendsId(profileId,offset,limit);
        return new ResponseEntity<>(profileIds, HttpStatus.OK);
    }

    @PostMapping("/report")
    public ResponseEntity<Boolean> report(@RequestBody ReportDto reportDto) throws Exception{
        boolean reported = profileService.report(reportDto);
        if(reported) return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/delete-profile/{profileId}")
    public ResponseEntity<Boolean> deleteProfile(@PathVariable String profileId) throws Exception{
        boolean deleted = profileService.deleteProfile(profileId);
        if(deleted) return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUserName() throws Exception{
        return new ResponseEntity<>(profileService.getUsername(), HttpStatus.OK);
    }
}
