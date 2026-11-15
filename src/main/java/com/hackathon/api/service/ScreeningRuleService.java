package com.hackathon.api.service;

import com.hackathon.api.dto.request.screening.ScreeningRuleRequest;
import com.hackathon.api.dto.response.screening.ScreeningRuleResponse;

import java.util.List;

public interface ScreeningRuleService {
    ScreeningRuleResponse create(ScreeningRuleRequest request);

    List<ScreeningRuleResponse> getByRequisition(String requisitionId);

    ScreeningRuleResponse getById(String id);

    ScreeningRuleResponse revise(String id, ScreeningRuleRequest request);
}
