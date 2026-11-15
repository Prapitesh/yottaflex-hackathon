package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for candidate_skill.
 * A candidate can have each catalogue skill only once.
 */
@Embeddable
public class CandidateSkillId implements Serializable {

    @Column(name = "candidate_id", nullable = false, length = 30)
    private String candidateId;

    @Column(name = "skill_id", nullable = false, length = 30)
    private String skillId;

    protected CandidateSkillId() {
    }

    public CandidateSkillId(String candidateId, String skillId) {
        this.candidateId = candidateId;
        this.skillId = skillId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getSkillId() {
        return skillId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CandidateSkillId that)) {
            return false;
        }
        return Objects.equals(candidateId, that.candidateId)
                && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateId, skillId);
    }
}
