package com.hackathon.api.exception;

/**
 * Raised when a requested domain resource does not exist.
 * The global API handler translates this exception to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
