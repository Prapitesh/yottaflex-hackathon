package com.hackathon.api.controller;

import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.dto.request.requisition.RequisitionDecisionRequest;
import com.hackathon.api.dto.request.requisition.RequisitionRequest;
import com.hackathon.api.dto.response.requisition.RequisitionResponse;
import com.hackathon.api.service.JobRequisitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * HTTP boundary for job requisition use cases.
 * Lifecycle decisions are delegated to the application service.
 */
@RestController
@RequestMapping("/api/v1/requisitions")
public class JobRequisitionController {

    private final JobRequisitionService requisitionService;

    public JobRequisitionController(JobRequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    @PostMapping
    public ResponseEntity<RequisitionResponse> create(
            @Valid @RequestBody RequisitionRequest request) {

        RequisitionResponse created =
                requisitionService.createRequisition(request);

        URI location = URI.create("/api/v1/requisitions/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RequisitionResponse>> getAll(
            @RequestParam(required = false) RequisitionStatus status,
            @RequestParam(required = false) String departmentId) {

        return ResponseEntity.ok(
                requisitionService.getRequisitions(status, departmentId)
        );
    }

    @GetMapping("/{requisitionId}")
    public ResponseEntity<RequisitionResponse> getById(
            @PathVariable String requisitionId) {

        return ResponseEntity.ok(
                requisitionService.getRequisitionById(requisitionId)
        );
    }

    @PutMapping("/{requisitionId}")
    public ResponseEntity<RequisitionResponse> updateDraft(
            @PathVariable String requisitionId,
            @Valid @RequestBody RequisitionRequest request) {

        return ResponseEntity.ok(
                requisitionService.updateDraft(requisitionId, request)
        );
    }

    @PostMapping("/{requisitionId}/submit")
    public ResponseEntity<RequisitionResponse> submit(
            @PathVariable String requisitionId,
            @Valid @RequestBody RequisitionDecisionRequest request) {

        return ResponseEntity.ok(
                requisitionService.submitForApproval(requisitionId, request)
        );
    }

    @PostMapping("/{requisitionId}/approve")
    public ResponseEntity<RequisitionResponse> approve(
            @PathVariable String requisitionId,
            @Valid @RequestBody RequisitionDecisionRequest request) {

        return ResponseEntity.ok(
                requisitionService.approve(requisitionId, request)
        );
    }

    @PostMapping("/{requisitionId}/reject")
    public ResponseEntity<RequisitionResponse> reject(
            @PathVariable String requisitionId,
            @Valid @RequestBody RequisitionDecisionRequest request) {

        return ResponseEntity.ok(
                requisitionService.reject(requisitionId, request)
        );
    }

    @PostMapping("/{requisitionId}/close")
    public ResponseEntity<RequisitionResponse> close(
            @PathVariable String requisitionId,
            @Valid @RequestBody RequisitionDecisionRequest request) {

        return ResponseEntity.ok(
                requisitionService.close(requisitionId, request)
        );
    }
}

