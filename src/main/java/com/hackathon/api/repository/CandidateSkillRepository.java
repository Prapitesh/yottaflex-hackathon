package com.hackathon.api.repository;

import com.hackathon.api.entity.CandidateSkill;
import com.hackathon.api.entity.CandidateSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for candidate-to-skill evidence records.
 */
public interface CandidateSkillRepository
        extends JpaRepository<CandidateSkill, CandidateSkillId> {

    List<CandidateSkill> findByCandidate_Id(String candidateId);

    List<CandidateSkill> findBySkill_Id(String skillId);

    boolean existsByCandidate_IdAndSkill_Id(String candidateId, String skillId);
}
