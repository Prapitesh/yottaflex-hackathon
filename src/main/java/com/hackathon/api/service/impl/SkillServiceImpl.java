package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.skill.SkillRequest;
import com.hackathon.api.dto.response.skill.SkillResponse;
import com.hackathon.api.entity.Skill;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.SkillRepository;
import com.hackathon.api.service.AuditTrailService;
import com.hackathon.api.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {
    private final SkillRepository repository;
    private final AuditTrailService auditTrailService;

    public SkillServiceImpl(SkillRepository repository, AuditTrailService auditTrailService) {
        this.repository = repository;
        this.auditTrailService = auditTrailService;
    }

    @Override
    public SkillResponse create(SkillRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Skill already exists: " + name);
        }
        Skill saved = repository.save(new Skill(
                "SKL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                name, request.category().trim()));
        auditTrailService.record("SKILL", saved.getId(), "SKILL_CREATED", "SYSTEM",
                Map.of("name", saved.getName(), "category", saved.getCategory()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getById(String id) {
        return toResponse(find(id));
    }

    @Override
    public SkillResponse update(String id, SkillRequest request) {
        Skill skill = find(id);
        repository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Skill already exists: " + request.name());
                });
        skill.updateDetails(request.name().trim(), request.category().trim());
        Skill saved = repository.save(skill);
        auditTrailService.record("SKILL", saved.getId(), "SKILL_UPDATED", "SYSTEM",
                Map.of("name", saved.getName(), "category", saved.getCategory()));
        return toResponse(saved);
    }

    @Override
    public void delete(String id) {
        Skill skill = find(id);
        auditTrailService.record("SKILL", id, "SKILL_DELETE_REQUESTED", "SYSTEM",
                Map.of("name", skill.getName()));
        repository.delete(skill);
    }

    private Skill find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }

    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory());
    }
}
