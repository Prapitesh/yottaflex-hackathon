package com.hackathon.api.dto.request.feedback;

import com.hackathon.api.enums.FeedbackRecommendation;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Structured interview assessment submitted by an assigned interviewer. */
public record SubmitFeedbackRequest(
        @NotBlank(message = "Interviewer ID is required")
        @Size(max = 40)
        String interviewerId,
        @NotNull(message = "Score is required")
        @DecimalMin(value = "0.00", message = "Score cannot be below 0")
        @DecimalMax(value = "10.00", message = "Score cannot exceed 10")
        BigDecimal score,
        @NotNull(message = "Recommendation is required")
        FeedbackRecommendation recommendation,
        @NotBlank(message = "Strengths are required")
        @Size(max = 5000)
        String strengths,
        @Size(max = 5000)
        String concerns
) {
}
