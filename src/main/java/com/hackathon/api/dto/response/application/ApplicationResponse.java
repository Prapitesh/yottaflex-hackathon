package com.hackathon.api.dto.response.application;

import com.hackathon.api.enums.ApplicationStage;
import java.time.OffsetDateTime;

/** Application-specific view, separate from the candidate profile. */
public record ApplicationResponse(
        String id,
        String requisitionId,
        String candidateId,
        String resumeId,
        ApplicationStage stage,
        OffsetDateTime appliedAt,
        OffsetDateTime lastStageChangedAt
) {
}
