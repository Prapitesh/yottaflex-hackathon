package com.hackathon.api.service.impl;

import com.hackathon.api.service.CandidateService;

import com.hackathon.api.dto.request.candidate.CandidateRequest;
import com.hackathon.api.dto.response.candidate.CandidateResponse;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.Candidate;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Manages candidate master profiles independently from job applications.
 */
@Service
@Transactional
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final AuditEventRepository auditEventRepository;

    public CandidateServiceImpl(
            CandidateRepository candidateRepository,
            AuditEventRepository auditEventRepository) {
        this.candidateRepository = candidateRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public CandidateResponse createCandidate(CandidateRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (candidateRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("A candidate with this email already exists");
        }

        Candidate candidate = new Candidate(
                EntityIdGenerator.candidateId(),
                request.fullName().trim(),
                normalizedEmail,
                normalizeOptional(request.phone()),
                normalizeOptional(request.currentLocation()),
                request.totalExperienceMonths()
        );

        Candidate saved = candidateRepository.save(candidate);
        recordAudit(saved, "CANDIDATE_CREATED");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateResponse> getAllCandidates() {
        return candidateRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(String candidateId) {
        return toResponse(findCandidate(candidateId));
    }

    @Override
    public CandidateResponse updateCandidate(
            String candidateId, CandidateRequest request) {
        Candidate candidate = findCandidate(candidateId);
        String normalizedEmail = request.email().trim().toLowerCase();

        candidateRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(candidateId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("A candidate with this email already exists");
                });

        candidate.updateProfile(
                request.fullName().trim(),
                normalizedEmail,
                normalizeOptional(request.phone()),
                normalizeOptional(request.currentLocation()),
                request.totalExperienceMonths()
        );

        Candidate saved = candidateRepository.save(candidate);
        recordAudit(saved, "CANDIDATE_UPDATED");
        return toResponse(saved);
    }

    private Candidate findCandidate(String id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found: " + id));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void recordAudit(Candidate candidate, String eventType) {
        auditEventRepository.save(new AuditEvent(
                "CANDIDATE",
                candidate.getId(),
                eventType,
                "SYSTEM",
                Map.of("email", candidate.getEmail())
        ));
    }

    private CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getCurrentLocation(),
                candidate.getTotalExperienceMonths(),
                candidate.getCreatedAt()
        );
    }
}
