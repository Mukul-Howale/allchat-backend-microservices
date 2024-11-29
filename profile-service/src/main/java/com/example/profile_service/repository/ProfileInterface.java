package com.example.profile_service.repository;

import com.example.profile_service.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileInterface extends MongoRepository<Profile, String> {
}
