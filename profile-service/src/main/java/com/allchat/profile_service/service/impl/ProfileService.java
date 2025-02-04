package com.allchat.profile_service.service.impl;

import com.allchat.profile_service.dto.ProfileResponseDto;
import com.allchat.profile_service.dto.ReportDto;
import com.allchat.profile_service.exception.NoSuchProfileException;
import com.allchat.profile_service.model.Profile;
import com.allchat.profile_service.model.Report;
import com.allchat.profile_service.repository.ReportRepository;
import com.allchat.profile_service.service.IProfileService;
import com.allchat.profile_service.repository.ProfileRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileService {

    private final ProfileRepository profileRepository;
    private final ReportRepository reportRepository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     *
     * @param userId
     * @return
     * @throws Exception
     *
     * TO:DO
     * checking if the userId exists
     */
    public ProfileResponseDto createProfile(String userId) throws Exception {
        try{

            Profile profile = Profile.builder()
                    .totalFriends(new BigInteger("0"))
                    .username(generateUsername())
                    .paid(false)
                    .likes(new BigInteger("0"))
                    .dislikes(new BigInteger("0"))
                    .profilePictureURL("")
                    .userId(userId)
                    .build();
            profileRepository.save(profile);
            log.info("Profile created");
            return modelMapper.map(profile,ProfileResponseDto.class);
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

    public List<String> getFriendIds(String profileId, Integer offset, Integer limit) throws Exception{
        try {
            Optional<String> friends = profileRepository.getFriendIds(profileId,offset,limit);
            if (friends.isEmpty()) {
                log.error("method : getFriends(String profileId, Integer offset, Integer limit)," +
                        "message : no profile found");
                throw new Exception("No profile ids found");
            }
            log.info("profile ids fetched");
            return Collections.singletonList(friends.get());
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public Boolean report(ReportDto reportDto) throws Exception{
        try{
            Report report = modelMapper.map(reportDto, Report.class);
            reportRepository.save(report);
            log.info("report saved");
            return true;
        }
        catch (Exception e){
            log.error(e.toString());
            throw new Exception();
        }
    }

    public Boolean deleteProfile(String profileId) throws Exception{
        try {
            Optional<Profile> profile = profileRepository.findById(profileId);
            if (profile.isEmpty()) {
                log.error("method : deleteProfile(String profileId)," + "message : no profile found");
                throw new Exception("No profile ids found");
            }
            log.info("profile ids fetched");
            profileRepository.deleteById(profileId);
            log.info("profile deleted");
            return true;
        }
        catch (Exception e){
            throw new Exception();
        }
    }

    public String getUsername() throws Exception { return  generateUsername(); }

    private String generateUsername() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(new URI("https://usernameapiv1.vercel.app/api/random-usernames").toURL()).path("usernames");
        StringBuilder username = new StringBuilder();
        if(jsonNode.isArray()){
            for(JsonNode node : jsonNode){
                username.append(node.asText());
            }
        }
        return username.toString();
    }
}