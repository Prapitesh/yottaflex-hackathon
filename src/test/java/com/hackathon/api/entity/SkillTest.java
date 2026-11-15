package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillTest {

    @Test
    void createsSkillWithIdentityNameAndCategory() {
        Skill skill = new Skill("SKILL-001", "Java", "PROGRAMMING_LANGUAGE");

        assertEquals("SKILL-001", skill.getId());
        assertEquals("Java", skill.getName());
        assertEquals("PROGRAMMING_LANGUAGE", skill.getCategory());
    }

    @Test
    void updatesSkillCatalogueDetails() {
        Skill skill = new Skill("SKILL-001", "Java", "PROGRAMMING_LANGUAGE");

        skill.updateDetails("Java 21", "BACKEND_TECHNOLOGY");

        assertEquals("Java 21", skill.getName());
        assertEquals("BACKEND_TECHNOLOGY", skill.getCategory());
    }
}
