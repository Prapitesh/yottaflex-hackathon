package com.hackathon.api.dto.request.requisition;

import com.hackathon.api.enums.EmploymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request body for creating or editing a draft requisition.
 */
public record RequisitionRequest(

        @NotBlank(message = "Department ID is required")
        @Size(max = 30)
        String departmentId,

        @NotBlank(message = "Job title is required")
        @Size(max = 160)
        String title,

        @NotBlank(message = "Job description is required")
        String description,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        @NotBlank(message = "Job location is required")
        @Size(max = 160)
        String location,

        @NotNull(message = "Number of openings is required")
        @Min(value = 1, message = "At least one opening is required")
        Integer openings,

        @NotNull(message = "Minimum salary is required")
        @DecimalMin(value = "0.00", message = "Minimum salary cannot be negative")
        @Digits(integer = 10, fraction = 2)
        BigDecimal salaryMin,

        @NotNull(message = "Maximum salary is required")
        @DecimalMin(value = "0.00", message = "Maximum salary cannot be negative")
        @Digits(integer = 10, fraction = 2)
        BigDecimal salaryMax,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase ISO code")
        String currency,

        @NotBlank(message = "Requester identity is required")
        @Size(max = 120)
        String requestedBy

) {
    @AssertTrue(message = "Maximum salary must be greater than or equal to minimum salary")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null
                || salaryMax.compareTo(salaryMin) >= 0;
    }
}

