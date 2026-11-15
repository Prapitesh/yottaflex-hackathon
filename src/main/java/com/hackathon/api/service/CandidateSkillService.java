package com.hackathon.api.service;
import com.hackathon.api.dto.request.candidate.CandidateSkillRequest;
import com.hackathon.api.dto.response.candidate.CandidateSkillResponse;
import java.util.List;
public interface CandidateSkillService {
 CandidateSkillResponse add(CandidateSkillRequest request);
 List<CandidateSkillResponse> getByCandidate(String candidateId);
 CandidateSkillResponse update(CandidateSkillRequest request);
 void remove(String candidateId,String skillId);
}