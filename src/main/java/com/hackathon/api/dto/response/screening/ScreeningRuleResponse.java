package com.hackathon.api.dto.response.screening;

import com.hackathon.api.enums.ScreeningRuleType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScreeningRuleResponse(
        String id,
        String requisitionId,
        String skillId,
        ScreeningRuleType ruleType,
        BigDecimal weight,
        BigDecimal threshold,
        boolean mandatory,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
