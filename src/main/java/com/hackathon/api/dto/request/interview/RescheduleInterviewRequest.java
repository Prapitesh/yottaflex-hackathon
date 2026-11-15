package com.hackathon.api.dto.request.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record RescheduleInterviewRequest(
        @NotNull(message = "New start time is required")
        OffsetDateTime scheduledStart,
        @NotNull(message = "New end time is required")
        OffsetDateTime scheduledEnd,
        @NotBlank(message = "Actor is required") @Size(max = 120)
        String actor,
        @NotBlank(message = "Reason is required") @Size(max = 1000)
        String reason
) {
}
