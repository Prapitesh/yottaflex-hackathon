package com.hackathon.api.controller;

import com.hackathon.api.dto.request.skill.SkillRequest;
import com.hackathon.api.dto.response.skill.SkillResponse;
import com.hackathon.api.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    private final SkillService service;

    public SkillController(SkillService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        SkillResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/skills/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<SkillResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public SkillResponse update(
            @PathVariable String id,
            @Valid @RequestBody SkillRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
