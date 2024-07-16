package com.coledit.backend.exceptions;

/**
 * Custom exception class to handle cases where an email is already in use.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    /**
     * Constructor for EmailAlreadyInUseException.
     * 
     * @param message the detail message about the exception.
     */
    public EmailAlreadyInUseException(String message) {
        super(message); // Call the superclass constructor with the provided message.
    }
}
