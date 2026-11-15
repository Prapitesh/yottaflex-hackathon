package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.skill.SkillRequest;
import com.hackathon.api.dto.response.skill.SkillResponse;
import com.hackathon.api.entity.Skill;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.SkillRepository;
import com.hackathon.api.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Service
@Transactional
public class SkillServiceImpl implements SkillService {
 private final SkillRepository repository;
 public SkillServiceImpl(SkillRepository repository){this.repository=repository;}
 public SkillResponse create(SkillRequest r){
  String name=r.name().trim();
  if(repository.existsByNameIgnoreCase(name)) throw new DuplicateResourceException("Skill already exists: "+name);
  return toResponse(repository.save(new Skill("SKL-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(),name,r.category().trim())));
 }
 @Transactional(readOnly=true) public List<SkillResponse> getAll(){return repository.findAll().stream().map(this::toResponse).toList();}
 @Transactional(readOnly=true) public SkillResponse getById(String id){return toResponse(find(id));}
 public SkillResponse update(String id,SkillRequest r){
  Skill s=find(id);
  repository.findByNameIgnoreCase(r.name().trim()).filter(x->!x.getId().equals(id)).ifPresent(x->{throw new DuplicateResourceException("Skill already exists: "+r.name());});
  s.updateDetails(r.name().trim(),r.category().trim()); return toResponse(repository.save(s));
 }
 public void delete(String id){repository.delete(find(id));}
 private Skill find(String id){return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Skill not found: "+id));}
 private SkillResponse toResponse(Skill s){return new SkillResponse(s.getId(),s.getName(),s.getCategory());}
}