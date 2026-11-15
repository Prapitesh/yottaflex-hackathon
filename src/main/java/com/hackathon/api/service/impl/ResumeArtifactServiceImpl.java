package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.resume.ResumeArtifactRequest;
import com.hackathon.api.dto.response.resume.ResumeArtifactResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.ResumeArtifactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @Transactional
public class ResumeArtifactServiceImpl implements ResumeArtifactService {
 private final ResumeArtifactRepository resumes; private final CandidateRepository candidates; private final ApplicationRepository applications;
 public ResumeArtifactServiceImpl(ResumeArtifactRepository r,CandidateRepository c,ApplicationRepository a){resumes=r;candidates=c;applications=a;}
 public ResumeArtifactResponse create(ResumeArtifactRequest r){
  Candidate c=candidates.findById(r.candidateId()).orElseThrow(()->new ResourceNotFoundException("Candidate not found: "+r.candidateId()));
  if(r.applicationId()!=null&&!r.applicationId().isBlank()){
   Application a=applications.findById(r.applicationId()).orElseThrow(()->new ResourceNotFoundException("Application not found: "+r.applicationId()));
   if(!a.getCandidate().getId().equals(c.getId()))throw new IllegalArgumentException("Resume candidate must match application candidate");
  }
  ResumeArtifact saved=resumes.save(new ResumeArtifact("RES-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(),c,r.applicationId(),r.fileName().trim(),r.storageUri().trim(),r.mimeType().trim(),r.checksumSha256().toLowerCase()));
  return map(saved);
 }
 @Transactional(readOnly=true) public List<ResumeArtifactResponse> getByCandidate(String id){if(!candidates.existsById(id))throw new ResourceNotFoundException("Candidate not found: "+id);return resumes.findByCandidate_IdOrderByUploadedAtDesc(id).stream().map(this::map).toList();}
 @Transactional(readOnly=true) public ResumeArtifactResponse getById(String id){return map(resumes.findById(id).orElseThrow(()->new ResourceNotFoundException("Resume not found: "+id)));}
 private ResumeArtifactResponse map(ResumeArtifact r){return new ResumeArtifactResponse(r.getId(),r.getCandidate().getId(),r.getApplicationId(),r.getFileName(),r.getStorageUri(),r.getMimeType(),r.getChecksumSha256(),r.getParsedAt(),r.getUploadedAt());}
}