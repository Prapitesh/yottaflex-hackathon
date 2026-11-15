package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.candidate.CandidateSkillRequest;
import com.hackathon.api.dto.response.candidate.CandidateSkillResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.exception.*;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.CandidateSkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @Transactional
public class CandidateSkillServiceImpl implements CandidateSkillService {
 private final CandidateSkillRepository links; private final CandidateRepository candidates; private final SkillRepository skills;
 public CandidateSkillServiceImpl(CandidateSkillRepository l,CandidateRepository c,SkillRepository s){links=l;candidates=c;skills=s;}
 public CandidateSkillResponse add(CandidateSkillRequest r){
  Candidate c=candidates.findById(r.candidateId()).orElseThrow(()->new ResourceNotFoundException("Candidate not found: "+r.candidateId()));
  Skill s=skills.findById(r.skillId()).orElseThrow(()->new ResourceNotFoundException("Skill not found: "+r.skillId()));
  if(links.existsByCandidate_IdAndSkill_Id(c.getId(),s.getId())) throw new DuplicateResourceException("Candidate already has this skill");
  return map(links.save(new CandidateSkill(c,s,r.proficiency(),r.yearsExperience(),r.evidenceSource(),r.verified())));
 }
 @Transactional(readOnly=true) public List<CandidateSkillResponse> getByCandidate(String id){if(!candidates.existsById(id))throw new ResourceNotFoundException("Candidate not found: "+id);return links.findByCandidate_Id(id).stream().map(this::map).toList();}
 public CandidateSkillResponse update(CandidateSkillRequest r){
  CandidateSkill cs=links.findById(new CandidateSkillId(r.candidateId(),r.skillId())).orElseThrow(()->new ResourceNotFoundException("Candidate skill not found"));
  cs.updateEvidence(r.proficiency(),r.yearsExperience(),r.evidenceSource(),r.verified());return map(links.save(cs));
 }
 public void remove(String candidateId,String skillId){CandidateSkill cs=links.findById(new CandidateSkillId(candidateId,skillId)).orElseThrow(()->new ResourceNotFoundException("Candidate skill not found"));links.delete(cs);}
 private CandidateSkillResponse map(CandidateSkill cs){return new CandidateSkillResponse(cs.getCandidate().getId(),cs.getSkill().getId(),cs.getSkill().getName(),cs.getProficiency(),cs.getYearsExperience(),cs.getEvidenceSource(),cs.isVerified());}
}