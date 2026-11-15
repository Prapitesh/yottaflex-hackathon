package com.hackathon.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundExceptionReturns404() {
        var response = handler.handleNotFound(
                new ResourceNotFoundException("Candidate not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Candidate not found", response.getBody().message());
    }

    @Test
    void duplicateExceptionReturns409() {
        var response = handler.handleDuplicate(
                new DuplicateResourceException("Email already exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void invalidArgumentReturns400() {
        var response = handler.handleBadArgument(
                new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void businessStateConflictReturns409() {
        var response = handler.handleBusinessConflict(
                new IllegalStateException("Invalid lifecycle transition"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalDetails() {
        var response = handler.handleUnexpected(
                new RuntimeException("SELECT * FROM private_table"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().message().contains("private_table"));
    }
}
