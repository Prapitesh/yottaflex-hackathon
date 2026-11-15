package com.hackathon.api.dto.response.feedback;

import com.hackathon.api.enums.FeedbackRecommendation;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Read model for one interviewer assessment. */
public record FeedbackResponse(
        String id,
        String interviewId,
        String interviewerId,
        String interviewerName,
        BigDecimal score,
        FeedbackRecommendation recommendation,
        String strengths,
        String concerns,
        OffsetDateTime submittedAt
) {
}
