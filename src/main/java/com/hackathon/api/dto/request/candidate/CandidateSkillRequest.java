package com.hackathon.api.dto.request.candidate;
import com.hackathon.api.enums.CandidateSkillProficiency;
import com.hackathon.api.enums.SkillEvidenceSource;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CandidateSkillRequest(@NotBlank String candidateId,@NotBlank String skillId,@NotNull CandidateSkillProficiency proficiency,@NotNull @DecimalMin("0.0") BigDecimal yearsExperience,@NotNull SkillEvidenceSource evidenceSource,boolean verified) {}