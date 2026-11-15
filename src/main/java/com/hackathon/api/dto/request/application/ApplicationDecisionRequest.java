package com.hackathon.api.dto.request.application;

import com.hackathon.api.enums.ApplicationStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Recruiter decision after screening or interview review. */
public record ApplicationDecisionRequest(
        @NotNull(message = "Decision stage is required")
        ApplicationStage targetStage,
        @NotBlank(message = "Actor is required") @Size(max = 120)
        String actor,
        @NotBlank(message = "Decision reason is required") @Size(max = 1000)
        String reason
) { }
