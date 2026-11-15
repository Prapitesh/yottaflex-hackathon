package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void capturesAuditEventAndPayload() {
        AuditEvent event = new AuditEvent(
                "APPLICATION", "APP-1", "STAGE_CHANGED", "Recruiter",
                Map.of("from", "APPLIED", "to", "SCREENING")
        );
        event.onCreate();

        assertEquals("APPLICATION", event.getAggregateType());
        assertEquals("STAGE_CHANGED", event.getEventType());
        assertEquals("SCREENING", event.getEventPayload().get("to"));
        assertNotNull(event.getOccurredAt());
    }
}
