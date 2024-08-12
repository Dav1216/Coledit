package com.coledit.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coledit.backend.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Find a user by their id
    Optional<User> findByUserId(UUID userId);
    // Find a user by their email
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.userId IN :ids")
    List<User> findAllByUserId(@Param("ids") List<UUID> userIds);
}
