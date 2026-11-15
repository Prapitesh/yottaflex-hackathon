package com.hackathon.api.controller;

import com.hackathon.api.dto.response.AuditEventResponse;
import com.hackathon.api.service.AuditTrailService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-events")
@Validated
public class AuditEventController {
    private final AuditTrailService auditTrailService;

    public AuditEventController(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    @GetMapping
    public List<AuditEventResponse> getEvents(
            @RequestParam @NotBlank @Size(max = 60) String aggregateType,
            @RequestParam @NotBlank @Size(max = 100) String aggregateId) {
        return auditTrailService.findByAggregate(aggregateType, aggregateId)
                .stream().map(AuditEventResponse::from).toList();
    }

    @GetMapping("/by-type")
    public List<AuditEventResponse> getByType(
            @RequestParam @NotBlank @Size(max = 80) String eventType) {
        return auditTrailService.findByEventType(eventType)
                .stream().map(AuditEventResponse::from).toList();
    }
}
