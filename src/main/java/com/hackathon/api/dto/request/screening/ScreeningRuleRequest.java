package com.hackathon.api.dto.request.screening;
import com.hackathon.api.enums.ScreeningRuleType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ScreeningRuleRequest(@NotBlank String requisitionId,String skillId,@NotNull ScreeningRuleType ruleType,@NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal weight,@DecimalMin("0.00") BigDecimal threshold,boolean mandatory,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo) {}