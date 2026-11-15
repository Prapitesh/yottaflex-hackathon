package com.hackathon.api.controller;

import com.hackathon.api.dto.request.candidate.CandidateSkillRequest;
import com.hackathon.api.dto.response.candidate.CandidateSkillResponse;
import com.hackathon.api.service.CandidateSkillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidate-skills")
public class CandidateSkillController {
    private final CandidateSkillService service;

    public CandidateSkillController(CandidateSkillService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CandidateSkillResponse> add(
            @Valid @RequestBody CandidateSkillRequest request) {
        return ResponseEntity.status(201).body(service.add(request));
    }

    @GetMapping("/candidate/{candidateId}")
    public List<CandidateSkillResponse> get(@PathVariable String candidateId) {
        return service.getByCandidate(candidateId);
    }

    @PutMapping
    public CandidateSkillResponse update(@Valid @RequestBody CandidateSkillRequest request) {
        return service.update(request);
    }

    @DeleteMapping("/{candidateId}/{skillId}")
    public ResponseEntity<Void> remove(
            @PathVariable String candidateId,
            @PathVariable String skillId) {
        service.remove(candidateId, skillId);
        return ResponseEntity.noContent().build();
    }
}
