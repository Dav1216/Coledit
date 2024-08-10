package com.coledit.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.coledit.backend.entities.User;
import com.coledit.backend.exceptions.UserNotFoundException;
import com.coledit.backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// The configuration of the application
@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Defines a UserDetailsService bean that retrieves user details from
     * UserRepository based on the username (email) and returns a UserDetails
     * object.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("UserDetailsService bean: User not found with email:" + username));
            if (user != null) {
                return user;
            }

            return null;
        };
    }

    /**
     * Provides a BCryptPasswordEncoder bean for encoding passwords.
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines an AuthenticationManager bean using the provided
     * AuthenticationConfiguration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines an AuthenticationProvider bean that uses the UserDetailsService
     * and BCryptPasswordEncoder to authenticate users.
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Bean
    @Scope("singleton")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        return mapper;
    }
}
