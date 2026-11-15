package com.hackathon.api.dto.request.screening;

import com.hackathon.api.enums.ScreeningRuleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScreeningRuleRequest(
        @NotBlank String requisitionId,
        String skillId,
        @NotNull ScreeningRuleType ruleType,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal weight,
        @DecimalMin("0.00") BigDecimal threshold,
        boolean mandatory,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
