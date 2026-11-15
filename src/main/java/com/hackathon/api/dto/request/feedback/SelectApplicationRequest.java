package com.hackathon.api.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Actor and reason for moving a fully assessed application to SELECTED. */
public record SelectApplicationRequest(
        @NotBlank(message = "Actor is required")
        @Size(max = 120)
        String actor,
        @NotBlank(message = "Selection reason is required")
        @Size(max = 1000)
        String reason
) {
}
