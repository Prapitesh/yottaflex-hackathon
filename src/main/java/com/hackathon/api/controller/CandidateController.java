package com.hackathon.api.controller;

import com.hackathon.api.dto.request.candidate.CandidateRequest;
import com.hackathon.api.dto.response.candidate.CandidateResponse;
import com.hackathon.api.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * HTTP boundary for candidate profile management.
 * Job-specific applications and decisions belong to separate resources.
 */
@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> create(
            @Valid @RequestBody CandidateRequest request) {

        CandidateResponse created = candidateService.createCandidate(request);
        URI location = URI.create("/api/v1/candidates/" + created.id());

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAll() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @GetMapping("/{candidateId}")
    public ResponseEntity<CandidateResponse> getById(
            @PathVariable String candidateId) {

        return ResponseEntity.ok(candidateService.getCandidateById(candidateId));
    }

    @PutMapping("/{candidateId}")
    public ResponseEntity<CandidateResponse> update(
            @PathVariable String candidateId,
            @Valid @RequestBody CandidateRequest request) {

        return ResponseEntity.ok(
                candidateService.updateCandidate(candidateId, request)
        );
    }
}
