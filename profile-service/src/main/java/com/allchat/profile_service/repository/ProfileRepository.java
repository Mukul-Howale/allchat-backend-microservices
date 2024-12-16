package com.allchat.profile_service.repository;

import com.allchat.profile_service.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<Profile, String> {
}
