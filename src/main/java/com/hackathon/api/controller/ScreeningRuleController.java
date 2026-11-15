package com.hackathon.api.controller;

import com.hackathon.api.dto.request.screening.ScreeningRuleRequest;
import com.hackathon.api.dto.response.screening.ScreeningRuleResponse;
import com.hackathon.api.service.ScreeningRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/screening-rules")
public class ScreeningRuleController {
    private final ScreeningRuleService service;

    public ScreeningRuleController(ScreeningRuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScreeningRuleResponse> create(
            @Valid @RequestBody ScreeningRuleRequest request) {
        ScreeningRuleResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/screening-rules/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ScreeningRuleResponse get(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/requisition/{requisitionId}")
    public List<ScreeningRuleResponse> byRequisition(@PathVariable String requisitionId) {
        return service.getByRequisition(requisitionId);
    }

    @PutMapping("/{id}")
    public ScreeningRuleResponse revise(
            @PathVariable String id,
            @Valid @RequestBody ScreeningRuleRequest request) {
        return service.revise(id, request);
    }
}
