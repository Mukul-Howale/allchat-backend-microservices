package com.allchat.profile_service.dto;

import lombok.Builder;

import java.math.BigInteger;

@Builder
public record ProfileResponseDto(String id, BigInteger friends, BigInteger likes,
                                 BigInteger dislikes, String profilePicturesURL) {
}
