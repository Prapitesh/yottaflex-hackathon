package com.hackathon.api.dto.response;

import java.time.OffsetDateTime;

/**
 * Stable response contract returned to API clients.
 * Internal persistence entities are not exposed directly.
 */
public record DepartmentResponse(
        String id,
        String name,
        String costCenter,
        OffsetDateTime createdAt
) {
}
