package com.example.profile_service.service;

import com.example.profile_service.dto.ProfileResponseDto;

public interface IProfileService {

    ProfileResponseDto createProfile(String userId) throws Exception;

    ProfileResponseDto getProfile(String profileId) throws Exception;
}
