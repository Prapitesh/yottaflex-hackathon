package com.hackathon.api.entity;

import com.hackathon.api.enums.ScreeningRuleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A versioned, requisition-specific criterion used to screen applicants. */
@Entity
@Table(name = "screening_rule")
public class ScreeningRule {

    @Id
    @NotBlank
    @Size(max = 40)
    @Column(name = "rule_id", nullable = false, length = 40)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisition_id", nullable = false)
    private JobRequisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 24)
    private ScreeningRuleType ruleType;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(precision = 8, scale = 2)
    private BigDecimal threshold;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    protected ScreeningRule() {
    }

    public ScreeningRule(
            String id,
            JobRequisition requisition,
            Skill skill,
            ScreeningRuleType ruleType,
            BigDecimal weight,
            BigDecimal threshold,
            boolean mandatory,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this.id = id;
        this.requisition = requisition;
        this.skill = skill;
        this.ruleType = ruleType;
        this.weight = weight == null ? BigDecimal.ZERO : weight;
        this.threshold = threshold;
        this.mandatory = mandatory;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public void revise(
            BigDecimal weight,
            BigDecimal threshold,
            boolean mandatory,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this.weight = weight == null ? BigDecimal.ZERO : weight;
        this.threshold = threshold;
        this.mandatory = mandatory;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    @AssertTrue(message = "Effective end date cannot be before effective start date")
    public boolean isEffectiveDateRangeValid() {
        return effectiveFrom == null || effectiveTo == null
                || !effectiveTo.isBefore(effectiveFrom);
    }

    @AssertTrue(message = "Skill rules require a skill; other rule types must not reference one")
    public boolean isSkillReferenceValid() {
        if (ruleType == null) {
            return true;
        }
        boolean skillRule = ruleType == ScreeningRuleType.REQUIRED_SKILL
                || ruleType == ScreeningRuleType.PREFERRED_SKILL;
        return skillRule == (skill != null);
    }

    public String getId() { return id; }
    public JobRequisition getRequisition() { return requisition; }
    public Skill getSkill() { return skill; }
    public ScreeningRuleType getRuleType() { return ruleType; }
    public BigDecimal getWeight() { return weight; }
    public BigDecimal getThreshold() { return threshold; }
    public boolean isMandatory() { return mandatory; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
}
