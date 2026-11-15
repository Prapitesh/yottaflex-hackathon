package com.hackathon.api.dto.response.requisition;

import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * API representation of a requisition; persistence entities stay internal.
 */
public record RequisitionResponse(
        String id,
        String departmentId,
        String title,
        String description,
        EmploymentType employmentType,
        String location,
        Integer openings,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        RequisitionStatus status,
        String requestedBy,
        String approvedBy,
        OffsetDateTime approvedAt,
        OffsetDateTime openedAt,
        OffsetDateTime closedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

