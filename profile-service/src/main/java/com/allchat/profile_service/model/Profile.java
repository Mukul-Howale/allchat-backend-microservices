package com.allchat.profile_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.List;

@Document(value = "profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {

    @Id
    private String id;

    private String userId;

    private BigInteger totalFriends;
    private BigInteger likes;
    private BigInteger dislikes;
    private boolean paid;
    private String profilePictureURL;

    private List<String> friends;
    private List<String> sentRequest;
    private List<String> receivedRequest;

    public void addFriend(String id){
        setTotalFriends(getTotalFriends().add(BigInteger.ONE));
        friends.add(id);
    }

    public void addSentRequest(String id){ sentRequest.add(id); }

    public void removeSentRequest(String id){ sentRequest.remove(id); }

    public void addReceivedRequest(String id){ receivedRequest.add(id); }

    public void removeReceivedRequest(String id){ receivedRequest.remove(id); }
}
