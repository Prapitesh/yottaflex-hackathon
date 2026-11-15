package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataIssueTest {

    @Test
    void recordsGroundTruthAndCanBeResolved() {
        DataIssue issue = new DataIssue(
                "INTERVIEWER_CONFLICT", "INTERVIEW", "INT-1",
                "KNOWN_CONFLICT", "Reject scheduling due to interviewer overlap",
                Map.of("interviewerId", "INTV-1")
        );
        issue.onCreate();
        OffsetDateTime resolvedAt = issue.getDetectedAt().plusHours(2);
        issue.resolve(resolvedAt);

        assertEquals("KNOWN_CONFLICT", issue.getGroundTruthLabel());
        assertEquals(resolvedAt, issue.getResolvedAt());
    }

    @Test
    void rejectsResolutionBeforeDetection() {
        DataIssue issue = new DataIssue(
                "DUPLICATE_APPLICATION", "APPLICATION", "APP-1",
                "DUPLICATE", "Flag duplicate application", Map.of()
        );
        issue.onCreate();

        assertThrows(IllegalArgumentException.class,
                () -> issue.resolve(issue.getDetectedAt().minusSeconds(1)));
    }
}
