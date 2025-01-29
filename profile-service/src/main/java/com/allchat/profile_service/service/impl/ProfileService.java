package com.allchat.profile_service.service.impl;

import com.allchat.profile_service.dto.ProfileResponseDto;
import com.allchat.profile_service.exception.NoSuchProfileException;
import com.allchat.profile_service.model.Profile;
import com.allchat.profile_service.service.IProfileService;
import com.allchat.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileService {

    private final ProfileRepository profileRepository;

    private final ModelMapper modelMapper;

    public ProfileResponseDto createProfile(String userId) throws Exception {
        try{
            Profile profile = Profile.builder()
                    .totalFriends(new BigInteger("0"))
                    .paid(false)
                    .likes(new BigInteger("0"))
                    .dislikes(new BigInteger("0"))
                    .profilePictureURL("")
                    .userId(userId)
                    .build();
            profileRepository.save(profile);
            log.info("Profile created");
            return new ProfileResponseDto(profile.getId(),profile.getTotalFriends(),
                    profile.getLikes(), profile.getDislikes(), profile.getProfilePictureURL());
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public ProfileResponseDto getProfile(String profileId) throws Exception {
        try{
            Optional<Profile> optionalProfile = profileRepository.findById(profileId);
            if(optionalProfile.isEmpty()){
                throw new NoSuchProfileException("No profile found");
            }
            log.info("Profile fetched");
            return modelMapper.map(optionalProfile.get(), ProfileResponseDto.class);
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean addLike(String profileId) throws Exception{
        try{
            Optional<Profile> optionalProfile = profileRepository.findById(profileId);
            if(optionalProfile.isEmpty()){
                log.error("method : addLike(String profileId), message : no profile found");
                throw new Exception("No profile found");
            }
            log.info("profile fetched");
            optionalProfile.get().setLikes(optionalProfile.get().getLikes().add(BigInteger.ONE));
            profileRepository.save(optionalProfile.get());
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean addDislike(String profileId) throws Exception{
        try{
            Optional<Profile> optionalProfile = profileRepository.findById(profileId);
            if(optionalProfile.isEmpty()){
                log.error("method : addDislike(String profileId), message : no profile found");
                throw new Exception("No profile found");
            }
            log.info("profile fetched");
            optionalProfile.get().setDislikes(optionalProfile.get().getDislikes().add(BigInteger.ONE));
            profileRepository.save(optionalProfile.get());
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean sendFriendRequest(String fromId, String toId) throws Exception{
        try{
            Optional<Profile> fromProfile = profileRepository.findById(fromId);
            Optional<Profile> toProfile = profileRepository.findById(toId);
            if (fromProfile.isEmpty() || toProfile.isEmpty()){
                log.error("method : sendFriendRequest(String fromId, String toId), message : no profile found");
                throw new Exception("No profile found");
            }
            log.info("profile fetched");
            fromProfile.get().addSentRequest(toId);
            toProfile.get().addReceivedRequest(fromId);
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean acceptFriendRequest(String fromId, String toId) throws Exception{
        try{
            Optional<Profile> fromProfile = profileRepository.findById(fromId);
            Optional<Profile> toProfile = profileRepository.findById(toId);
            if (fromProfile.isEmpty() || toProfile.isEmpty()){
                log.error("method : acceptFriendRequest(String fromId, String toId), message : no profile found");
                throw new Exception("No profile found");
            }
            log.info("profile fetched");
            fromProfile.get().addFriend(toId);
            toProfile.get().addFriend(fromId);
            fromProfile.get().removeSentRequest(toId);
            toProfile.get().removeReceivedRequest(fromId);
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean removeFriend(String profileId, String friendId) throws Exception{
        try{
            Optional<Profile> optionalProfile = profileRepository.findById(profileId);
            Optional<Profile> optionalFriend = profileRepository.findById(friendId);
            if (optionalProfile.isEmpty() || optionalFriend.isEmpty()){
                log.error("method : removeFriend(String profileId, String friendId), message : no profile found");
                throw new Exception("No profile found");
            }
            log.info("profile fetched");
            optionalProfile.get().removeFriend(friendId);
            optionalFriend.get().removeFriend(profileId);
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }
    // Remove friend
    // Block user
    // Report user
    // Unblock user 
    // Get user's profile
    // Get all profiles
    // Update profile
    // Delete profile
    // Get all users
    // Get user's friends
    // Get user's likes
    // Get user's dislikes
    // Add like
    // Add dislike
}