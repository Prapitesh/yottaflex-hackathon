package com.hackathon.api.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Consistent error response returned by the API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {
}
