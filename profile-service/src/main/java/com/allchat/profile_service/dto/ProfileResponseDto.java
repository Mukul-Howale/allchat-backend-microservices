package com.allchat.profile_service.dto;

import lombok.Builder;

import java.math.BigInteger;
import java.util.List;

@Builder
public record ProfileResponseDto(String id, String userId, String username, BigInteger totalFriends, BigInteger likes,
                                 BigInteger dislikes, boolean paid, String profilePictureURL,
                                 List<String> friends, List<String> sentRequest, List<String> receivedRequest) {
}
