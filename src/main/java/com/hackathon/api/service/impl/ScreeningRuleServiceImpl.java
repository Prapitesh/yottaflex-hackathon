package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.screening.ScreeningRuleRequest;
import com.hackathon.api.dto.response.screening.ScreeningRuleResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.ScreeningRuleType;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.ScreeningRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @Transactional
public class ScreeningRuleServiceImpl implements ScreeningRuleService {
 private final ScreeningRuleRepository rules; private final JobRequisitionRepository requisitions; private final SkillRepository skills;
 public ScreeningRuleServiceImpl(ScreeningRuleRepository r,JobRequisitionRepository q,SkillRepository s){rules=r;requisitions=q;skills=s;}
 public ScreeningRuleResponse create(ScreeningRuleRequest r){
  JobRequisition q=requisitions.findById(r.requisitionId()).orElseThrow(()->new ResourceNotFoundException("Requisition not found: "+r.requisitionId()));
  Skill skill=resolveSkill(r);
  validateDates(r);
  ScreeningRule saved=rules.save(new ScreeningRule("RULE-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(),q,skill,r.ruleType(),r.weight(),r.threshold(),r.mandatory(),r.effectiveFrom(),r.effectiveTo()));
  return map(saved);
 }
 @Transactional(readOnly=true) public List<ScreeningRuleResponse> getByRequisition(String id){
  if(!requisitions.existsById(id))throw new ResourceNotFoundException("Requisition not found: "+id);
  return rules.findByRequisition_IdOrderByEffectiveFromAsc(id).stream().map(this::map).toList();
 }
 @Transactional(readOnly=true) public ScreeningRuleResponse getById(String id){return map(find(id));}
 public ScreeningRuleResponse revise(String id,ScreeningRuleRequest r){
  ScreeningRule rule=find(id);
  if(!rule.getRequisition().getId().equals(r.requisitionId()))throw new IllegalArgumentException("A screening rule cannot be moved to another requisition; create a new version instead");
  validateDates(r);
  rule.revise(r.weight(),r.threshold(),r.mandatory(),r.effectiveFrom(),r.effectiveTo());
  return map(rules.save(rule));
 }
 private Skill resolveSkill(ScreeningRuleRequest r){
  boolean needsSkill=r.ruleType()==ScreeningRuleType.REQUIRED_SKILL||r.ruleType()==ScreeningRuleType.PREFERRED_SKILL;
  if(needsSkill!=(r.skillId()!=null&&!r.skillId().isBlank()))throw new IllegalArgumentException("Only required/preferred skill rules must include a skillId");
  if(!needsSkill)return null;
  return skills.findById(r.skillId()).orElseThrow(()->new ResourceNotFoundException("Skill not found: "+r.skillId()));
 }
 private void validateDates(ScreeningRuleRequest r){if(r.effectiveTo()!=null&&r.effectiveTo().isBefore(r.effectiveFrom()))throw new IllegalArgumentException("Effective end date cannot be before start date");}
 private ScreeningRule find(String id){return rules.findById(id).orElseThrow(()->new ResourceNotFoundException("Screening rule not found: "+id));}
 private ScreeningRuleResponse map(ScreeningRule x){return new ScreeningRuleResponse(x.getId(),x.getRequisition().getId(),x.getSkill()==null?null:x.getSkill().getId(),x.getRuleType(),x.getWeight(),x.getThreshold(),x.isMandatory(),x.getEffectiveFrom(),x.getEffectiveTo());}
}