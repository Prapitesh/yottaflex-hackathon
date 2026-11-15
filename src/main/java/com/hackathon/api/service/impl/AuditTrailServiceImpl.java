package com.hackathon.api.service.impl;

import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.service.AuditTrailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditTrailServiceImpl implements AuditTrailService {
    private final AuditEventRepository repository;

    public AuditTrailServiceImpl(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEvent record(String aggregateType, String aggregateId, String eventType,
                             String actor, Map<String, Object> payload) {
        return repository.save(new AuditEvent(aggregateType, aggregateId, eventType, actor, payload));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findByAggregate(String aggregateType, String aggregateId) {
        return repository.findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc(
                aggregateType.trim().toUpperCase(), aggregateId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> findByEventType(String eventType) {
        return repository.findByEventTypeOrderByOccurredAtDesc(eventType.trim().toUpperCase());
    }
}
