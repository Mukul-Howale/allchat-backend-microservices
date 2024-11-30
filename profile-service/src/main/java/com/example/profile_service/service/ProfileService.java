package com.example.profile_service.service;

import com.example.profile_service.dto.ProfileResponse;
import com.example.profile_service.model.Profile;
import com.example.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponse createProfile(String userId) {
        Profile profile = Profile.builder()
                .friends(new BigInteger("0"))
                .likes(new BigInteger("0"))
                .dislikes(new BigInteger("0"))
                .profilePictureURL("")
                .userId(userId)
                .build();
        profileRepository.save(profile);
        log.info("Profile created");
        return new ProfileResponse(profile.getId(),profile.getFriends(),
                profile.getLikes(), profile.getDislikes(), profile.getProfilePictureURL());
    }
}
