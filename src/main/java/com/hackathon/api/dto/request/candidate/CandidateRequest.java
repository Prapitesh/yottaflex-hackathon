package com.hackathon.api.dto.request.candidate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * API input for a candidate profile.
 * Do not include screening decisions or application stage in this profile DTO.
 */
public record CandidateRequest(

        @NotBlank(message = "Candidate name is required")
        @Size(max = 160, message = "Name must not exceed 160 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254)
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 160, message = "Location must not exceed 160 characters")
        String currentLocation,

        @Min(value = 0, message = "Experience cannot be negative")
        Integer totalExperienceMonths

) {
}
