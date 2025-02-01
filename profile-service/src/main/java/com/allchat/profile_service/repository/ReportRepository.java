package com.allchat.profile_service.repository;

import com.allchat.profile_service.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
}
