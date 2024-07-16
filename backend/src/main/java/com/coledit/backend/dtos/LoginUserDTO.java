package com.coledit.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for logging in a user.
 * This class encapsulates user login information such as email and password.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {
    // The email address of the user.
    private String email;
    // The password provided by the user.
    private String password; 
}