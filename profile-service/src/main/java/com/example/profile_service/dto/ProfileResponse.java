package com.example.profile_service.dto;

import java.math.BigInteger;

public record ProfileResponse(String id, BigInteger friends, BigInteger likes,
                              BigInteger dislikes, String profilePicturesURL) {
}
