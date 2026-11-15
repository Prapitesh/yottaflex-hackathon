package com.hackathon.api.dto.response.candidate;

import java.time.OffsetDateTime;

/**
 * Safe candidate profile response.
 * Application-specific screening and interview information is returned
 * by the corresponding application/interview APIs, not this endpoint.
 */
public record CandidateResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String currentLocation,
        Integer totalExperienceMonths,
        OffsetDateTime createdAt
) {
}
