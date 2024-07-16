package com.coledit.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coledit.backend.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Find a user by their email
    User findByUserId(UUID userId);
    // Find a user by their email
    User findByEmail(String email);
}
