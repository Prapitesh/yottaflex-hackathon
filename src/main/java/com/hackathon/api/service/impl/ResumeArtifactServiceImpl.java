package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.resume.ResumeArtifactRequest;
import com.hackathon.api.dto.response.resume.ResumeArtifactResponse;
import com.hackathon.api.entity.Application;
import com.hackathon.api.entity.Candidate;
import com.hackathon.api.entity.ResumeArtifact;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.ApplicationRepository;
import com.hackathon.api.repository.CandidateRepository;
import com.hackathon.api.repository.ResumeArtifactRepository;
import com.hackathon.api.service.AuditTrailService;
import com.hackathon.api.service.ResumeArtifactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ResumeArtifactServiceImpl implements ResumeArtifactService {
    private final ResumeArtifactRepository resumes;
    private final CandidateRepository candidates;
    private final ApplicationRepository applications;
    private final AuditTrailService auditTrailService;

    public ResumeArtifactServiceImpl(ResumeArtifactRepository resumes, CandidateRepository candidates,
                                     ApplicationRepository applications, AuditTrailService auditTrailService) {
        this.resumes = resumes;
        this.candidates = candidates;
        this.applications = applications;
        this.auditTrailService = auditTrailService;
    }

    @Override
    public ResumeArtifactResponse create(ResumeArtifactRequest request) {
        Candidate candidate = candidates.findById(request.candidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + request.candidateId()));
        if (request.applicationId() != null && !request.applicationId().isBlank()) {
            Application application = applications.findById(request.applicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + request.applicationId()));
            if (!application.getCandidate().getId().equals(candidate.getId())) {
                throw new IllegalArgumentException("Resume candidate must match application candidate");
            }
        }
        ResumeArtifact saved = resumes.save(new ResumeArtifact(
                "RES-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(),
                candidate, request.applicationId(), request.fileName().trim(), request.storageUri().trim(),
                request.mimeType().trim(), request.checksumSha256().toLowerCase()));
        auditTrailService.record("RESUME_ARTIFACT", saved.getId(), "RESUME_METADATA_CREATED", "SYSTEM",
                Map.of("candidateId", candidate.getId(), "fileName", saved.getFileName(),
                        "mimeType", saved.getMimeType(), "checksumSha256", saved.getChecksumSha256()));
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeArtifactResponse> getByCandidate(String id) {
        if (!candidates.existsById(id)) {
            throw new ResourceNotFoundException("Candidate not found: " + id);
        }
        return resumes.findByCandidate_IdOrderByUploadedAtDesc(id).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeArtifactResponse getById(String id) {
        return map(resumes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id)));
    }

    private ResumeArtifactResponse map(ResumeArtifact resume) {
        return new ResumeArtifactResponse(resume.getId(), resume.getCandidate().getId(),
                resume.getApplicationId(), resume.getFileName(), resume.getStorageUri(),
                resume.getMimeType(), resume.getChecksumSha256(), resume.getParsedAt(), resume.getUploadedAt());
    }
}
