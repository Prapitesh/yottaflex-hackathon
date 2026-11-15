package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CandidateTest {

    private Candidate newCandidate(Integer experienceMonths) {
        return new Candidate(
                "CAND-000001",
                "Aarav Sharma",
                "aarav@example.com",
                "+91-9000000000",
                "Hyderabad",
                experienceMonths
        );
    }

    @Test
    void newCandidateStoresProfileAndDefaultsNullExperienceToZero() {
        Candidate candidate = newCandidate(null);

        assertEquals("CAND-000001", candidate.getId());
        assertEquals("Aarav Sharma", candidate.getFullName());
        assertEquals("aarav@example.com", candidate.getEmail());
        assertEquals(0, candidate.getTotalExperienceMonths());
        assertNull(candidate.getCreatedAt());
    }

    @Test
    void updateProfileChangesOnlyCandidateProfileFields() {
        Candidate candidate = newCandidate(12);

        candidate.updateProfile(
                "Aarav S.",
                "aarav.s@example.com",
                null,
                "Bengaluru",
                18
        );

        assertEquals("Aarav S.", candidate.getFullName());
        assertEquals("aarav.s@example.com", candidate.getEmail());
        assertNull(candidate.getPhone());
        assertEquals("Bengaluru", candidate.getCurrentLocation());
        assertEquals(18, candidate.getTotalExperienceMonths());
    }

    @Test
    void lifecycleCallbackSetsCreationTimestampAndKeepsExistingTimestamp() {
        Candidate candidate = newCandidate(6);

        candidate.onCreate();
        OffsetDateTime createdAt = candidate.getCreatedAt();

        assertNotNull(createdAt);

        candidate.onCreate();

        assertEquals(createdAt, candidate.getCreatedAt());
    }

    @Test
    void updateProfileDefaultsNullExperienceToZero() {
        Candidate candidate = newCandidate(6);

        candidate.updateProfile(
                "Aarav Sharma",
                "aarav@example.com",
                null,
                null,
                null
        );

        assertEquals(0, candidate.getTotalExperienceMonths());
    }
}
