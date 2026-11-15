package com.hackathon.api.service;
import com.hackathon.api.dto.request.skill.SkillRequest;
import com.hackathon.api.dto.response.skill.SkillResponse;
import java.util.List;
public interface SkillService {
 SkillResponse create(SkillRequest request);
 List<SkillResponse> getAll();
 SkillResponse getById(String id);
 SkillResponse update(String id, SkillRequest request);
 void delete(String id);
}