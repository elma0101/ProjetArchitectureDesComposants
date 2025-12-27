package com.bookstore.recommendation.repository;

import com.bookstore.recommendation.entity.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {
    
    Optional<UserPreference> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}
