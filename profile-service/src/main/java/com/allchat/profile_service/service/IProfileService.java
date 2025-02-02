package com.allchat.profile_service.service;

import com.allchat.profile_service.dto.ProfileResponseDto;
import com.allchat.profile_service.dto.ReportDto;
import com.allchat.profile_service.model.Profile;

import java.util.List;

public interface IProfileService {

    ProfileResponseDto createProfile(String userId) throws Exception;

    ProfileResponseDto getProfile(String profileId) throws Exception;

    Boolean addLike(String profileId) throws Exception;

    Boolean addDislike(String profileId) throws Exception;

    Boolean sendFriendRequest(String fromId, String toId) throws Exception;

    Boolean acceptFriendRequest(String fromId, String toId) throws Exception;

    Boolean removeFriend(String profileId, String friendId) throws Exception;

    List<String> getFriendsId(String profileId, Integer offset, Integer limit) throws Exception;

    Boolean report(ReportDto reportDto) throws Exception;

    Boolean deleteProfile(String profileId) throws Exception;

    String getUsername() throws Exception;
}
