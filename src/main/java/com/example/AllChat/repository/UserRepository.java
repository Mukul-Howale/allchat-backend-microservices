package com.example.AllChat.repository;

import com.example.AllChat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(
            nativeQuery = true,
            value = "SELECT * FROM allchat.`user` u WHERE u.username = :username")
    Optional<User> existsByUsername(@Param("email") String username);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM allchat.`user` u WHERE u.email = :email")
    Optional<User> existsByEmail(@Param("email") String email);
}
