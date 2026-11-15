package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResumeArtifactTest {

    private Candidate candidate() {
        return new Candidate(
                "CAND-000001",
                "Aarav Sharma",
                "aarav@example.com",
                null,
                "Hyderabad",
                12
        );
    }

    private ResumeArtifact resume() {
        return new ResumeArtifact(
                "RES-000001",
                candidate(),
                null,
                "aarav-resume.pdf",
                "s3://recruitment/resumes/RES-000001.pdf",
                "application/pdf",
                "a".repeat(64)
        );
    }

    @Test
    void resumeBelongsToCandidateAndCanExistBeforeApplication() {
        ResumeArtifact resume = resume();

        assertEquals("CAND-000001", resume.getCandidate().getId());
        assertNull(resume.getApplicationId());
        assertEquals("aarav-resume.pdf", resume.getFileName());
    }

    @Test
    void canLinkResumeToApplicationAfterUpload() {
        ResumeArtifact resume = resume();

        resume.linkToApplication("APP-REQ-2026-000001-CAND-000001");

        assertEquals(
                "APP-REQ-2026-000001-CAND-000001",
                resume.getApplicationId()
        );
    }

    @Test
    void uploadCallbackSetsTimestampOnlyOnce() {
        ResumeArtifact resume = resume();

        resume.onCreate();
        OffsetDateTime uploadedAt = resume.getUploadedAt();
        resume.onCreate();

        assertNotNull(uploadedAt);
        assertEquals(uploadedAt, resume.getUploadedAt());
    }

    @Test
    void parsingRequiresTimestampNotEarlierThanUpload() {
        ResumeArtifact resume = resume();
        resume.onCreate();

        assertThrows(
                IllegalArgumentException.class,
                () -> resume.markParsed(resume.getUploadedAt().minusSeconds(1))
        );

        OffsetDateTime parsedAt = resume.getUploadedAt().plusMinutes(2);
        resume.markParsed(parsedAt);

        assertEquals(parsedAt, resume.getParsedAt());
    }

    @Test
    void parsingRejectsNullTimestamp() {
        ResumeArtifact resume = resume();

        assertThrows(IllegalArgumentException.class, () -> resume.markParsed(null));
    }
}
