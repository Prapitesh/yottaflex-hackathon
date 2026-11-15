package com.hackathon.api.service.impl;

import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuditTrailServiceImplTest {
    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditTrailServiceImpl service = new AuditTrailServiceImpl(repository);

    @Test
    void recordPersistsAnImmutableAuditEvent() {
        AuditEvent event = mock(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenReturn(event);

        AuditEvent result = service.record("APPLICATION", "APP-1", "APPLICATION_CREATED",
                "recruiter@example.test", Map.of("stage", "APPLIED"));

        assertThat(result).isSameAs(event);
        verify(repository).save(argThat(saved ->
                saved.getAggregateType().equals("APPLICATION")
                        && saved.getAggregateId().equals("APP-1")
                        && saved.getEventType().equals("APPLICATION_CREATED")
                        && saved.getActor().equals("recruiter@example.test")
                        && saved.getEventPayload().get("stage").equals("APPLIED")));
    }

    @Test
    void findByAggregateNormalizesAggregateType() {
        when(repository.findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc("APPLICATION", "APP-1"))
                .thenReturn(List.of());

        assertThat(service.findByAggregate(" application ", "APP-1")).isEmpty();
        verify(repository).findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc("APPLICATION", "APP-1");
    }

    @Test
    void findByEventTypeNormalizesEventType() {
        when(repository.findByEventTypeOrderByOccurredAtDesc("OFFER_APPROVED")).thenReturn(List.of());

        assertThat(service.findByEventType(" offer_approved ")).isEmpty();
        verify(repository).findByEventTypeOrderByOccurredAtDesc("OFFER_APPROVED");
    }
}
