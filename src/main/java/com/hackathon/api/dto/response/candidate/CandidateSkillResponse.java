package com.hackathon.api.dto.response.candidate;
import com.hackathon.api.enums.CandidateSkillProficiency;
import com.hackathon.api.enums.SkillEvidenceSource;
import java.math.BigDecimal;
public record CandidateSkillResponse(String candidateId,String skillId,String skillName,CandidateSkillProficiency proficiency,BigDecimal yearsExperience,SkillEvidenceSource evidenceSource,boolean verified) {}