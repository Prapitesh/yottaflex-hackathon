package com.hackathon.api.dto.request.interview;

import com.hackathon.api.enums.InterviewPanelRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InterviewPanelistRequest(
        @NotBlank(message = "Interviewer ID is required") @Size(max = 40)
        String interviewerId,
        @NotNull(message = "Panel role is required")
        InterviewPanelRole role
) {
}
