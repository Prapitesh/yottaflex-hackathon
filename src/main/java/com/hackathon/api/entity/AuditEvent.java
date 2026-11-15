package com.hackathon.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

/** Generic audit trail entry; payload is stored as PostgreSQL JSONB. */
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "aggregate_type", nullable = false, length = 60)
    private String aggregateType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String actor;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> eventPayload;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(String aggregateType, String aggregateId, String eventType,
                      String actor, Map<String, Object> eventPayload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.actor = actor;
        this.eventPayload = eventPayload == null ? Map.of() : Map.copyOf(eventPayload);
    }

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getActor() { return actor; }
    public Map<String, Object> getEventPayload() { return eventPayload; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
}
