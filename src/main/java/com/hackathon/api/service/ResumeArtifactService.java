package com.hackathon.api.service;
import com.hackathon.api.dto.request.resume.ResumeArtifactRequest;
import com.hackathon.api.dto.response.resume.ResumeArtifactResponse;
import java.util.List;
public interface ResumeArtifactService { ResumeArtifactResponse create(ResumeArtifactRequest r); List<ResumeArtifactResponse> getByCandidate(String candidateId); ResumeArtifactResponse getById(String id); }