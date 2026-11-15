package com.hackathon.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * HTTP request contract for creating or updating a department.
 * Validation here protects the API boundary before business processing.
 */
public record DepartmentRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 120, message = "Department name must not exceed 120 characters")
        String name,

        @Size(max = 40, message = "Cost center must not exceed 40 characters")
        String costCenter

) {
}
