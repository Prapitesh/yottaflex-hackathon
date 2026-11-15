package com.hackathon.api.controller;

import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.service.AuditTrailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditEventControllerTest {
    private FakeAuditTrailService service;
    private AuditEventController controller;

    @BeforeEach
    void setUp() {
        service = new FakeAuditTrailService();
        controller = new AuditEventController(service);
    }

    @Test
    void getEventsReturnsAggregateHistory() {
        assertEquals(1, controller.getEvents("APPLICATION", "APP-1").size());
        assertEquals("APP-1", service.lastAggregateId);
    }

    @Test
    void getByTypeReturnsMatchingEvents() {
        assertEquals(1, controller.getByType("OFFER_APPROVED").size());
        assertEquals("OFFER_APPROVED", service.lastEventType);
    }

    private static class FakeAuditTrailService implements AuditTrailService {
        private String lastAggregateId;
        private String lastEventType;

        @Override
        public AuditEvent record(String aggregateType, String aggregateId, String eventType,
                                 String actor, Map<String, Object> payload) {
            throw new UnsupportedOperationException("Not used by controller tests");
        }

        @Override
        public List<AuditEvent> findByAggregate(String aggregateType, String aggregateId) {
            lastAggregateId = aggregateId;
            return List.of(new AuditEvent("APPLICATION", aggregateId, "APPLICATION_CREATED",
                    "SYSTEM", Map.of("stage", "APPLIED")));
        }

        @Override
        public List<AuditEvent> findByEventType(String eventType) {
            lastEventType = eventType;
            return List.of(new AuditEvent("OFFER", "OFF-1", eventType,
                    "SYSTEM", Map.of("status", "APPROVED")));
        }
    }
}
