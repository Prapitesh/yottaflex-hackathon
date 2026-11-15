package com.hackathon.api.dto.request.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Input for applying a candidate to a requisition. Stage and screening results are server-controlled. */
public record ApplicationRequest(
        @NotBlank(message = "Requisition ID is required") @Size(max = 30)
        String requisitionId,
        @NotBlank(message = "Candidate ID is required") @Size(max = 30)
        String candidateId,
        @Size(max = 40)
        String resumeId
) {
}
