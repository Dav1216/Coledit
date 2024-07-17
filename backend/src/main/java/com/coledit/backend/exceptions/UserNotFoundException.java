package com.coledit.backend.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message); // Call the superclass constructor with the provided message.
    }
}
