package com.hackathon.api.dto.response.screening;

import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.ScreeningDecision;

import java.math.BigDecimal;
import java.util.List;

/**
 * Explainable result of evaluating an application against its requisition rules.
 */
public record ScreeningResult(
        String applicationId,
        BigDecimal score,
        ScreeningDecision decision,
        ApplicationStage stage,
        List<String> reasons
) {
    public ScreeningResult {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
