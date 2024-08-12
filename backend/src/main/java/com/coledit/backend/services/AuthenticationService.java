package com.coledit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.coledit.backend.dtos.AuthenticationUserDTO;
import com.coledit.backend.dtos.UserDTO;
import com.coledit.backend.entities.User;
import com.coledit.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;

import com.coledit.backend.exceptions.EmailAlreadyInUseException;
import com.coledit.backend.exceptions.UserNotFoundException;

/**
 * Service class for handling user authentication and registration.
 */
@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Value("${custom.hostname}")
    private String hostname;

    /**
     * Constructor for AuthenticationService.
     * 
     * @param userRepository        the repository for user data
     * @param authenticationManager the manager for authentication operations
     * @param passwordEncoder       the encoder for password hashing
     * @param jwtService            the service for JWT operations
     * @param emailService          the service for email operations
     */
    @Autowired
    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user.
     * 
     * @param input the data transfer object containing registration details
     * @return the registered user
     * @throws EmailAlreadyInUseException if the email is already in use
     */
    @Transactional
    public User signup(AuthenticationUserDTO input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("The email address is already in use: " + input.getEmail());
        }

        User user = User.builder()
                .email(input.getEmail())
                .hashPassword(passwordEncoder.encode(input.getPassword()))
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticates a user.
     * 
     * @param input the data transfer object containing login details
     * @return the authenticated user
     */
    public User authenticate(AuthenticationUserDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()));

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + input.getEmail()));
        
    }
}