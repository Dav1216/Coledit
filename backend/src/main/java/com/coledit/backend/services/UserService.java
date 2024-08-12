package com.coledit.backend.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.coledit.backend.entities.User;
import com.coledit.backend.repositories.UserRepository;
import com.coledit.backend.exceptions.EmailAlreadyInUseException;
import com.coledit.backend.exceptions.UserNotFoundException;

import jakarta.transaction.Transactional;

/**
 * Service class that provides methods to manage User entities.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor to initialize UserService with required dependencies.
     *
     * @param userRepository         The repository for User entities.
     * @param requestService         The service for managing requests.
     * @param refreshTokenRepository The repository for refresh tokens.
     * @param passwordEncoder        The encoder for password hashing.
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param id The ID of the user.
     * @return The user with the specified ID.
     */
    public User getUser(String id) {
        return userRepository.findByUserId(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User getUserByUUID(UUID id) {
        return userRepository.findByUserId(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Retrieves a user by their email address.
     * 
     * @param email The email address of the user.
     * @return The user with the specified email address.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Updates a user with the specified ID.
     * 
     * @param id      The ID of the user to update.
     * @param newUser The updated user object.
     * @return The updated user object.
     */

    @Transactional
    public User updateUser(String id, User newUser) {
        newUser.setUserId(UUID.fromString(id));
        newUser.setHashPassword(passwordEncoder.encode(newUser.getHashPassword()));
        return userRepository.save(newUser);
    }

    /**
     * Updates a user with the specified email address.
     * 
     * @param email   The email address of the user to update.
     * @param newUser The updated user object.
     * @return The updated user object.
     */

    @Transactional
    public User updateUserByEmail(String email, User newUser) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        newUser.setUserId(existingUser.getUserId());
        newUser.setHashPassword(passwordEncoder.encode(newUser.getHashPassword()));
        return userRepository.save(newUser);
    }

    /**
     * Adds a new user.
     * 
     * @param user The user object to add.
     * @return The added user object.
     */
    @Transactional
    public User addUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("The email address is already in use: " + user.getEmail());
        }
        user.setHashPassword(passwordEncoder.encode(user.getHashPassword()));
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID, along with associated requests and refresh
     * tokens.
     * 
     * @param id The ID of the user to delete.
     * @return A message indicating the success of the deletion.
     */
    @Transactional
    public String deleteUserById(String id) {
        userRepository.deleteById(UUID.fromString(id));
        return "User deleted.";
    }

    /**
     * Retrieves all users.
     * 
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByUserIds(List<UUID> userIds) {
        return userRepository.findAllByUserId(userIds);
    }

}
