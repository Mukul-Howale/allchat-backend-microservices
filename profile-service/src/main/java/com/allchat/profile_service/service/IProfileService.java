package com.allchat.profile_service.service;

import com.allchat.profile_service.dto.ProfileResponseDto;

public interface IProfileService {

    ProfileResponseDto createProfile(String userId) throws Exception;

    ProfileResponseDto getProfile(String profileId) throws Exception;

    Boolean addLike(String profileId) throws Exception;
}
