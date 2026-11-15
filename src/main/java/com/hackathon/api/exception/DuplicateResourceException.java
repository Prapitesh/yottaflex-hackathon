package com.hackathon.api.exception;

/**
 * Raised when creating or updating a resource would violate a uniqueness rule.
 * The global API handler translates this exception to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
