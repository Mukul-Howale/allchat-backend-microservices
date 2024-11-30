package com.example.profile_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;

@Document(value = "profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
    private String id;
    private BigInteger friends;
    private BigInteger likes;
    private BigInteger dislikes;
    //private boolean paid;
    private String profilePictureURL;

    private String userId;
}
