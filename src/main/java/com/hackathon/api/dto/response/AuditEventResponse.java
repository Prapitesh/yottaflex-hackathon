package com.hackathon.api.dto.response;

import com.hackathon.api.entity.AuditEvent;

import java.time.OffsetDateTime;
import java.util.Map;

public record AuditEventResponse(
        Long id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String actor,
        Map<String, Object> eventPayload,
        OffsetDateTime occurredAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getAggregateType(),
                event.getAggregateId(), event.getEventType(), event.getActor(),
                event.getEventPayload(), event.getOccurredAt());
    }
}
