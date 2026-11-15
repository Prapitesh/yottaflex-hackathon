package com.hackathon.api.dto.request.skill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SkillRequest(@NotBlank @Size(max=100) String name, @NotBlank @Size(max=60) String category) {}