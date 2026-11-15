package com.hackathon.api.dto.request.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewActionRequest(
        @NotBlank(message = "Actor is required") @Size(max = 120)
        String actor,
        @NotBlank(message = "Reason is required") @Size(max = 1000)
        String reason
) {
}
