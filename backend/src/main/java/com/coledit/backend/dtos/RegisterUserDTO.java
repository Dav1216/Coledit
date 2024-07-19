package com.coledit.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for registering a new user.
 * This class encapsulates user registration information such as email, password, and roles.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDTO {
    // Represents the inputed email address
    private String email;
    // Represents the inputed password
    private String password;
}