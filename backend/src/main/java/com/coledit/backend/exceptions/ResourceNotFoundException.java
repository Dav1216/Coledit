package com.coledit.backend.exceptions;

/**
 * Custom exception class to handle cases where a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructor for ResourceNotFoundException.
     * 
     * @param message the detail message about the exception.
     */
    public ResourceNotFoundException(String message) {
        super(message); // Call the superclass constructor with the provided message.
    }
}
