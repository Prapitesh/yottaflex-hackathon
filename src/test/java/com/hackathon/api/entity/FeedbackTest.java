package com.hackathon.api.entity;

import com.hackathon.api.enums.FeedbackRecommendation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackTest {

    @Test
    void createsStructuredFeedback() {
        Feedback feedback = new Feedback(
                "FB-INT-1-INTV-1", null, null, new BigDecimal("8.50"),
                FeedbackRecommendation.STRONG_YES, "Strong Java fundamentals", "Needs system design practice"
        );
        feedback.onCreate();

        assertEquals(new BigDecimal("8.50"), feedback.getScore());
        assertEquals(FeedbackRecommendation.STRONG_YES, feedback.getRecommendation());
        assertNotNull(feedback.getSubmittedAt());
    }

    @Test
    void revisesFeedback() {
        Feedback feedback = new Feedback(
                "FB-2", null, null, new BigDecimal("5.00"),
                FeedbackRecommendation.HOLD, "Some strengths", null
        );

        feedback.revise(new BigDecimal("7.50"), FeedbackRecommendation.YES,
                "Good problem solving", "Could improve communication");

        assertEquals(new BigDecimal("7.50"), feedback.getScore());
        assertEquals(FeedbackRecommendation.YES, feedback.getRecommendation());
        assertEquals("Could improve communication", feedback.getConcerns());
    }
}
