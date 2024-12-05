package com.example.profile_service.service.impl;

import com.example.profile_service.dto.ProfileResponseDto;
import com.example.profile_service.model.Profile;
import com.example.profile_service.repository.ProfileRepository;
import com.example.profile_service.service.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponseDto createProfile(String userId) throws Exception {
        try {
            Profile profile = Profile.builder()
                    .friends(new BigInteger("0"))
                    .likes(new BigInteger("0"))
                    .dislikes(new BigInteger("0"))
                    .profilePictureURL("")
                    .userId(userId)
                    .build();
            profileRepository.save(profile);
            log.info("Profile created");
            return new ProfileResponseDto(profile.getId(),profile.getFriends(),
                    profile.getLikes(), profile.getDislikes(), profile.getProfilePictureURL());
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public ProfileResponseDto getProfile(String profileId) throws Exception {
        try{
            Optional<Profile> profile = profileRepository.findById(profileId);
            if(profile.isEmpty()) {
                throw new Exception("No profile found");
            }
            log.info("Profile fetched");
            return new ProfileResponseDto(profile.get().getId(),profile.get().getFriends(),
                    profile.get().getLikes(), profile.get().getDislikes(), profile.get().getProfilePictureURL());
        }
        catch (Exception e){
            throw new Exception();
        }
    }
}
