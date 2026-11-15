package com.hackathon.api.service;

import com.hackathon.api.dto.request.candidate.CandidateRequest;
import com.hackathon.api.dto.response.candidate.CandidateResponse;

import java.util.List;

/**
 * Candidate profile use cases.
 * The implementation will enforce unique email and profile rules.
 */
public interface CandidateService {

    CandidateResponse createCandidate(CandidateRequest request);

    List<CandidateResponse> getAllCandidates();

    CandidateResponse getCandidateById(String candidateId);

    CandidateResponse updateCandidate(
            String candidateId, CandidateRequest request);
}
