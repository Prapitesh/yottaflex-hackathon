package com.hackathon.api.service;

import com.hackathon.api.entity.AuditEvent;

import java.util.List;
import java.util.Map;

public interface AuditTrailService {
    AuditEvent record(String aggregateType, String aggregateId, String eventType,
                      String actor, Map<String, Object> payload);

    List<AuditEvent> findByAggregate(String aggregateType, String aggregateId);

    List<AuditEvent> findByEventType(String eventType);
}
