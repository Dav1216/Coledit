package com.coledit.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coledit.backend.builders.CookieBuilder;
import com.coledit.backend.dtos.LoginUserDTO;
import com.coledit.backend.dtos.RegisterUserDTO;
import com.coledit.backend.entities.User;
import com.coledit.backend.repositories.UserRepository;
import com.coledit.backend.services.AuthenticationService;
import com.coledit.backend.services.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthenticationController handles user authentication and authorization
 * endpoints.
 */
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    /**
     * Constructor to initialize the AuthenticationController with necessary
     * services.
     *
     * @param jwtService            the JWT service
     * @param authenticationService the authentication service
     * @param userRepository        the user repository
     * @param refreshTokenService   the refresh token service
     */
    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new user.
     *
     * @param registerUserDto the registration data transfer object
     * @return the registered user or conflict status if user already exists
     */
    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDTO registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        if (registeredUser == null) {
            // User already exists
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(registeredUser);
        }

        return ResponseEntity.ok(registeredUser);
    }

    /**
     * Authenticates a user and sets JWT and refresh tokens in HTTPOnly cookies.
     *
     * @param loginUserDto the login data transfer object
     * @param response     the HTTP response
     * @return a success message or error status if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody LoginUserDTO loginUserDto, HttpServletResponse request,
            HttpServletResponse response) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            // Generate JWT token
            String jwtToken = jwtService.generateToken(authenticatedUser);

            // Set the JWT token in an HTTPOnly cookie
            Cookie token = new CookieBuilder()
                    .setName("token")
                    .setValue(jwtToken)
                    .setHttpOnly(true)
                    .setSecure(true)
                    .setPath("/")
                    .setMaxAge((int) jwtService.getExpirationTime())
                    .setSameSite("Lax")
                    .build();
            response.addCookie(token);

            // Set the role token in a simple cookie, not HTTPOnly
            Cookie roleCookie = new CookieBuilder()
                    .setName("role")
                    .setValue(authenticatedUser.getRoles())
                    .setHttpOnly(false)
                    .setSecure(true)
                    .setPath("/")
                    .setMaxAge((int) jwtService.getExpirationTime())
                    .setSameSite("Lax")
                    .build();
            response.addCookie(roleCookie);

            return ResponseEntity.ok("Successfully authenticated!");

        } catch (AuthenticationException e) {
            // Handle specific exceptions based on type
            if (e instanceof BadCredentialsException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            } else if (e instanceof LockedException || e instanceof DisabledException
                    || e instanceof AccountExpiredException || e instanceof CredentialsExpiredException) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is locked or disabled/expired");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An error occurred during authentication");
            }
        }
    }

    /**
     * Logs out a user by invalidating JWT and refresh tokens.
     *
     * @param response the HTTP response
     * @return a success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Set the invalidated JWT token in an HTTPOnly cookie
        Cookie token = new CookieBuilder()
                .setName("token")
                .setValue(null)
                .setHttpOnly(true)
                .setSecure(true)
                .setPath("/")
                .setMaxAge(0)
                .setSameSite("None")
                .build();
        response.addCookie(token);

        // Set the invalidated role token in a simple cookie, not HTTPOnly
        Cookie roleCookie = new CookieBuilder()
                .setName("role")
                .setValue(null)
                .setHttpOnly(false)
                .setSecure(true)
                .setPath("/")
                .setMaxAge(0)
                .build();
        response.addCookie(roleCookie);

        return ResponseEntity.ok("Successfully logged out!");
    }
}
