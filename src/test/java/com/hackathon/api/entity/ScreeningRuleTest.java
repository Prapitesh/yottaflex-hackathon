package com.hackathon.api.entity;

import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.ScreeningRuleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ScreeningRuleTest {

    @Test
    void createsRequiredSkillRuleForARequisition() {
        Department department = new Department("DEPT-1", "Engineering", "ENG");
        JobRequisition requisition = new JobRequisition(
                "REQ-2026-000001", department, "Java Developer", "Build APIs",
                EmploymentType.FULL_TIME, "Hyderabad", 2,
                new BigDecimal("500000"), new BigDecimal("900000"), "INR", "Hiring Manager"
        );
        Skill skill = new Skill("SKILL-1", "Java", "PROGRAMMING_LANGUAGE");

        ScreeningRule rule = new ScreeningRule(
                "RULE-1", requisition, skill, ScreeningRuleType.REQUIRED_SKILL,
                new BigDecimal("40.00"), new BigDecimal("2.00"), true,
                LocalDate.of(2026, 1, 1), null
        );

        assertEquals(requisition, rule.getRequisition());
        assertEquals(skill, rule.getSkill());
        assertEquals(ScreeningRuleType.REQUIRED_SKILL, rule.getRuleType());
        assertTrue(rule.isMandatory());
        assertTrue(rule.isSkillReferenceValid());
        assertTrue(rule.isEffectiveDateRangeValid());
    }

    @Test
    void rejectsReversedEffectiveDateRange() {
        ScreeningRule rule = new ScreeningRule(
                "RULE-2", null, null, ScreeningRuleType.MIN_EXPERIENCE,
                BigDecimal.ZERO, BigDecimal.ONE, true,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1)
        );

        assertFalse(rule.isEffectiveDateRangeValid());
    }

    @Test
    void nonSkillRuleCannotReferenceSkill() {
        Skill skill = new Skill("SKILL-1", "Java", "PROGRAMMING_LANGUAGE");
        ScreeningRule rule = new ScreeningRule(
                "RULE-3", null, skill, ScreeningRuleType.MIN_EXPERIENCE,
                BigDecimal.ZERO, BigDecimal.ONE, true,
                LocalDate.of(2026, 1, 1), null
        );

        assertFalse(rule.isSkillReferenceValid());
    }

    @Test
    void revisesRuleWithoutChangingItsIdentity() {
        ScreeningRule rule = new ScreeningRule(
                "RULE-4", null, null, ScreeningRuleType.LOCATION,
                BigDecimal.ZERO, null, false,
                LocalDate.of(2026, 1, 1), null
        );

        rule.revise(new BigDecimal("15.00"), new BigDecimal("1.00"), true,
                LocalDate.of(2026, 2, 1), null);

        assertEquals("RULE-4", rule.getId());
        assertEquals(new BigDecimal("15.00"), rule.getWeight());
        assertTrue(rule.isMandatory());
    }
}
