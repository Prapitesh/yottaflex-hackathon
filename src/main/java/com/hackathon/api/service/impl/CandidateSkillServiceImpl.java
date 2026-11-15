package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.candidate.CandidateSkillRequest;
import com.hackathon.api.dto.response.candidate.CandidateSkillResponse;
import com.hackathon.api.entity.Candidate;
import com.hackathon.api.entity.CandidateSkill;
import com.hackathon.api.entity.CandidateSkillId;
import com.hackathon.api.entity.Skill;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.CandidateRepository;
import com.hackathon.api.repository.CandidateSkillRepository;
import com.hackathon.api.repository.SkillRepository;
import com.hackathon.api.service.AuditTrailService;
import com.hackathon.api.service.CandidateSkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CandidateSkillServiceImpl implements CandidateSkillService {
    private final CandidateSkillRepository links;
    private final CandidateRepository candidates;
    private final SkillRepository skills;
    private final AuditTrailService auditTrailService;

    public CandidateSkillServiceImpl(CandidateSkillRepository links, CandidateRepository candidates,
                                     SkillRepository skills, AuditTrailService auditTrailService) {
        this.links = links;
        this.candidates = candidates;
        this.skills = skills;
        this.auditTrailService = auditTrailService;
    }

    @Override
    public CandidateSkillResponse add(CandidateSkillRequest request) {
        Candidate candidate = candidates.findById(request.candidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + request.candidateId()));
        Skill skill = skills.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.skillId()));
        if (links.existsByCandidate_IdAndSkill_Id(candidate.getId(), skill.getId())) {
            throw new DuplicateResourceException("Candidate already has this skill");
        }
        CandidateSkill saved = links.save(new CandidateSkill(candidate, skill, request.proficiency(),
                request.yearsExperience(), request.evidenceSource(), request.verified()));
        auditTrailService.record("CANDIDATE_SKILL", candidate.getId() + ":" + skill.getId(),
                "CANDIDATE_SKILL_ADDED", "SYSTEM",
                Map.of("candidateId", candidate.getId(), "skillId", skill.getId(),
                        "verified", request.verified(), "evidenceSource", request.evidenceSource().name()));
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateSkillResponse> getByCandidate(String id) {
        if (!candidates.existsById(id)) {
            throw new ResourceNotFoundException("Candidate not found: " + id);
        }
        return links.findByCandidate_Id(id).stream().map(this::map).toList();
    }

    @Override
    public CandidateSkillResponse update(CandidateSkillRequest request) {
        CandidateSkill skill = links.findById(new CandidateSkillId(request.candidateId(), request.skillId()))
                .orElseThrow(() -> new ResourceNotFoundException("Candidate skill not found"));
        skill.updateEvidence(request.proficiency(), request.yearsExperience(),
                request.evidenceSource(), request.verified());
        CandidateSkill saved = links.save(skill);
        auditTrailService.record("CANDIDATE_SKILL", request.candidateId() + ":" + request.skillId(),
                "CANDIDATE_SKILL_UPDATED", "SYSTEM",
                Map.of("verified", request.verified(), "evidenceSource", request.evidenceSource().name(),
                        "proficiency", request.proficiency().name()));
        return map(saved);
    }

    @Override
    public void remove(String candidateId, String skillId) {
        CandidateSkill skill = links.findById(new CandidateSkillId(candidateId, skillId))
                .orElseThrow(() -> new ResourceNotFoundException("Candidate skill not found"));
        auditTrailService.record("CANDIDATE_SKILL", candidateId + ":" + skillId,
                "CANDIDATE_SKILL_REMOVED", "SYSTEM",
                Map.of("candidateId", candidateId, "skillId", skillId));
        links.delete(skill);
    }

    private CandidateSkillResponse map(CandidateSkill skill) {
        return new CandidateSkillResponse(skill.getCandidate().getId(), skill.getSkill().getId(),
                skill.getSkill().getName(), skill.getProficiency(), skill.getYearsExperience(),
                skill.getEvidenceSource(), skill.isVerified());
    }
}
