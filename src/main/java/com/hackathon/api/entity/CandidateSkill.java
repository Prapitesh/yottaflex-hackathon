package com.hackathon.api.entity;

import com.hackathon.api.enums.CandidateSkillProficiency;
import com.hackathon.api.enums.SkillEvidenceSource;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Join entity between Candidate and Skill.
 * Stores attributes that belong to the relationship, not to either master.
 */
@Entity
@Table(name = "candidate_skill")
public class CandidateSkill {

    @EmbeddedId
    private CandidateSkillId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 16)
    private CandidateSkillProficiency proficiency;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "years_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsExperience = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_source", nullable = false, length = 30)
    private SkillEvidenceSource evidenceSource;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    /** Required by JPA. Use the public constructor in application code. */
    protected CandidateSkill() {
    }

    public CandidateSkill(
            Candidate candidate,
            Skill skill,
            CandidateSkillProficiency proficiency,
            BigDecimal yearsExperience,
            SkillEvidenceSource evidenceSource,
            boolean verified
    ) {
        if (candidate == null || skill == null) {
            throw new IllegalArgumentException("Candidate and skill are required");
        }
        this.candidate = candidate;
        this.skill = skill;
        this.id = new CandidateSkillId(candidate.getId(), skill.getId());
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience == null
                ? BigDecimal.ZERO : yearsExperience;
        this.evidenceSource = evidenceSource;
        this.verified = verified;
    }

    public void updateEvidence(
            CandidateSkillProficiency proficiency,
            BigDecimal yearsExperience,
            SkillEvidenceSource evidenceSource,
            boolean verified
    ) {
        if (yearsExperience != null && yearsExperience.signum() < 0) {
            throw new IllegalArgumentException("Years of experience cannot be negative");
        }
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience == null
                ? BigDecimal.ZERO : yearsExperience;
        this.evidenceSource = evidenceSource;
        this.verified = verified;
    }

    public CandidateSkillId getId() {
        return id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public Skill getSkill() {
        return skill;
    }

    public CandidateSkillProficiency getProficiency() {
        return proficiency;
    }

    public BigDecimal getYearsExperience() {
        return yearsExperience;
    }

    public SkillEvidenceSource getEvidenceSource() {
        return evidenceSource;
    }

    public boolean isVerified() {
        return verified;
    }
}
