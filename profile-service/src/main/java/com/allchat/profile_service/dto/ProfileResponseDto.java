package com.allchat.profile_service.dto;

import java.math.BigInteger;

public record ProfileResponseDto(String id, BigInteger friends, BigInteger likes,
                                 BigInteger dislikes, String profilePicturesURL) {
}
