package com.hackathon.api.repository;

import com.hackathon.api.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Read access to audit events for traceability and operational review.
 */
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc(
            String aggregateType,
            String aggregateId
    );

    List<AuditEvent> findByEventTypeOrderByOccurredAtDesc(String eventType);
}
