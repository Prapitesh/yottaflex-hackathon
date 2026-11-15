package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.screening.ScreeningRuleRequest;
import com.hackathon.api.dto.response.screening.ScreeningRuleResponse;
import com.hackathon.api.entity.JobRequisition;
import com.hackathon.api.entity.ScreeningRule;
import com.hackathon.api.entity.Skill;
import com.hackathon.api.enums.ScreeningRuleType;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.JobRequisitionRepository;
import com.hackathon.api.repository.ScreeningRuleRepository;
import com.hackathon.api.repository.SkillRepository;
import com.hackathon.api.service.AuditTrailService;
import com.hackathon.api.service.ScreeningRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ScreeningRuleServiceImpl implements ScreeningRuleService {
    private final ScreeningRuleRepository rules;
    private final JobRequisitionRepository requisitions;
    private final SkillRepository skills;
    private final AuditTrailService auditTrailService;

    public ScreeningRuleServiceImpl(ScreeningRuleRepository rules, JobRequisitionRepository requisitions,
                                    SkillRepository skills, AuditTrailService auditTrailService) {
        this.rules = rules;
        this.requisitions = requisitions;
        this.skills = skills;
        this.auditTrailService = auditTrailService;
    }

    @Override
    public ScreeningRuleResponse create(ScreeningRuleRequest request) {
        JobRequisition requisition = requisitions.findById(request.requisitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found: " + request.requisitionId()));
        Skill skill = resolveSkill(request);
        validateDates(request);
        ScreeningRule saved = rules.save(new ScreeningRule(
                "RULE-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(),
                requisition, skill, request.ruleType(), request.weight(), request.threshold(),
                request.mandatory(), request.effectiveFrom(), request.effectiveTo()));
        auditTrailService.record("SCREENING_RULE", saved.getId(), "SCREENING_RULE_CREATED", "SYSTEM",
                Map.of("requisitionId", requisition.getId(), "ruleType", request.ruleType().name(),
                        "mandatory", request.mandatory(), "weight", request.weight()));
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScreeningRuleResponse> getByRequisition(String id) {
        if (!requisitions.existsById(id)) {
            throw new ResourceNotFoundException("Requisition not found: " + id);
        }
        return rules.findByRequisition_IdOrderByEffectiveFromAsc(id).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScreeningRuleResponse getById(String id) {
        return map(find(id));
    }

    @Override
    public ScreeningRuleResponse revise(String id, ScreeningRuleRequest request) {
        ScreeningRule rule = find(id);
        if (!rule.getRequisition().getId().equals(request.requisitionId())) {
            throw new IllegalArgumentException("A screening rule cannot be moved to another requisition; create a new version instead");
        }
        validateDates(request);
        rule.revise(request.weight(), request.threshold(), request.mandatory(),
                request.effectiveFrom(), request.effectiveTo());
        ScreeningRule saved = rules.save(rule);
        auditTrailService.record("SCREENING_RULE", saved.getId(), "SCREENING_RULE_REVISED", "SYSTEM",
                Map.of("mandatory", request.mandatory(), "weight", request.weight(),
                        "effectiveFrom", request.effectiveFrom().toString(),
                        "effectiveTo", request.effectiveTo() == null ? "OPEN" : request.effectiveTo().toString()));
        return map(saved);
    }

    private Skill resolveSkill(ScreeningRuleRequest request) {
        boolean needsSkill = request.ruleType() == ScreeningRuleType.REQUIRED_SKILL
                || request.ruleType() == ScreeningRuleType.PREFERRED_SKILL;
        if (needsSkill != (request.skillId() != null && !request.skillId().isBlank())) {
            throw new IllegalArgumentException("Only required/preferred skill rules must include a skillId");
        }
        if (!needsSkill) {
            return null;
        }
        return skills.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.skillId()));
    }

    private void validateDates(ScreeningRuleRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new IllegalArgumentException("Effective end date cannot be before start date");
        }
    }

    private ScreeningRule find(String id) {
        return rules.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening rule not found: " + id));
    }

    private ScreeningRuleResponse map(ScreeningRule rule) {
        return new ScreeningRuleResponse(rule.getId(), rule.getRequisition().getId(),
                rule.getSkill() == null ? null : rule.getSkill().getId(), rule.getRuleType(),
                rule.getWeight(), rule.getThreshold(), rule.isMandatory(),
                rule.getEffectiveFrom(), rule.getEffectiveTo());
    }
}
