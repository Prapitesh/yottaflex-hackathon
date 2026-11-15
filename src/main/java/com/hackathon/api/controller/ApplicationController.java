package com.hackathon.api.controller;

import com.hackathon.api.dto.request.application.ApplicationDecisionRequest;
import com.hackathon.api.dto.request.application.ApplicationRequest;
import com.hackathon.api.dto.request.application.WithdrawApplicationRequest;
import com.hackathon.api.dto.response.application.ApplicationResponse;
import com.hackathon.api.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/** HTTP boundary for candidate-to-requisition applications. */
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse created = applicationService.createApplication(request);
        URI location = URI.create("/api/v1/applications/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAll(
            @RequestParam(required = false) String requisitionId,
            @RequestParam(required = false) String candidateId) {
        return ResponseEntity.ok(applicationService.getApplications(requisitionId, candidateId));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable String applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }

    @PostMapping("/{applicationId}/decision")
    public ResponseEntity<ApplicationResponse> decide(
            @PathVariable String applicationId,
            @Valid @RequestBody ApplicationDecisionRequest request) {
        return ResponseEntity.ok(applicationService.decideApplication(applicationId, request));
    }

    @PostMapping("/{applicationId}/withdraw")
    public ResponseEntity<ApplicationResponse> withdraw(
            @PathVariable String applicationId,
            @Valid @RequestBody WithdrawApplicationRequest request) {
        return ResponseEntity.ok(applicationService.withdrawApplication(
                applicationId, request.actor(), request.reason()));
    }
}
