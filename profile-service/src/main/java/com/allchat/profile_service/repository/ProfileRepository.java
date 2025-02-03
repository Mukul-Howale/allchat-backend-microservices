package com.allchat.profile_service.repository;

import com.allchat.profile_service.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends MongoRepository<Profile, String> {

    @Query(value = "{ 'profileId': ?0 }", fields = "{ 'friends': { $slice: [?1, ?2] }, '_id': 0 }")
    Optional<String> getFriendIds(String profileId, Integer offset, Integer limit);
}
