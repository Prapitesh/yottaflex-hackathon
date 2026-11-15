package com.hackathon.api.entity;

import com.hackathon.api.enums.CandidateSkillProficiency;
import com.hackathon.api.enums.SkillEvidenceSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CandidateSkillTest {

    private Candidate candidate() {
        return new Candidate(
                "CAND-000001", "Aarav Sharma", "aarav@example.com",
                null, "Hyderabad", 12
        );
    }

    private Skill skill() {
        return new Skill("SKILL-001", "Java", "PROGRAMMING_LANGUAGE");
    }

    @Test
    void createsCompositeIdentityFromCandidateAndSkill() {
        CandidateSkill candidateSkill = new CandidateSkill(
                candidate(), skill(), CandidateSkillProficiency.ADVANCED,
                new BigDecimal("2.5"), SkillEvidenceSource.ASSESSMENT, true
        );

        assertEquals("CAND-000001", candidateSkill.getId().getCandidateId());
        assertEquals("SKILL-001", candidateSkill.getId().getSkillId());
        assertEquals(CandidateSkillProficiency.ADVANCED, candidateSkill.getProficiency());
        assertEquals(new BigDecimal("2.5"), candidateSkill.getYearsExperience());
        assertTrue(candidateSkill.isVerified());
    }

    @Test
    void compositeIdsUseBothPartsForEquality() {
        CandidateSkillId first = new CandidateSkillId("CAND-1", "SKILL-1");
        CandidateSkillId same = new CandidateSkillId("CAND-1", "SKILL-1");
        CandidateSkillId different = new CandidateSkillId("CAND-1", "SKILL-2");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    void updatesSkillEvidence() {
        CandidateSkill candidateSkill = new CandidateSkill(
                candidate(), skill(), CandidateSkillProficiency.BEGINNER,
                BigDecimal.ZERO, SkillEvidenceSource.DECLARED, false
        );

        candidateSkill.updateEvidence(
                CandidateSkillProficiency.INTERMEDIATE,
                new BigDecimal("1.5"),
                SkillEvidenceSource.INTERVIEW,
                true
        );

        assertEquals(CandidateSkillProficiency.INTERMEDIATE, candidateSkill.getProficiency());
        assertEquals(new BigDecimal("1.5"), candidateSkill.getYearsExperience());
        assertEquals(SkillEvidenceSource.INTERVIEW, candidateSkill.getEvidenceSource());
        assertTrue(candidateSkill.isVerified());
    }

    @Test
    void rejectsNegativeYearsExperience() {
        CandidateSkill candidateSkill = new CandidateSkill(
                candidate(), skill(), CandidateSkillProficiency.BEGINNER,
                BigDecimal.ZERO, SkillEvidenceSource.DECLARED, false
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> candidateSkill.updateEvidence(
                        CandidateSkillProficiency.BEGINNER,
                        new BigDecimal("-0.1"),
                        SkillEvidenceSource.DECLARED,
                        false
                )
        );
    }

    @Test
    void requiresCandidateAndSkill() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CandidateSkill(
                        null, skill(), CandidateSkillProficiency.BEGINNER,
                        BigDecimal.ZERO, SkillEvidenceSource.DECLARED, false
                )
        );
    }
}
