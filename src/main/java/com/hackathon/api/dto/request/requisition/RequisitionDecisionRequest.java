package com.hackathon.api.dto.request.requisition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Explicit actor and reason for a lifecycle action.
 * The service will verify actor authority and allowed state transitions.
 */
public record RequisitionDecisionRequest(

        @NotBlank(message = "Actor identity is required")
        @Size(max = 120)
        String actor,

        @NotBlank(message = "A decision reason is required")
        @Size(max = 1000)
        String reason

) {
}
