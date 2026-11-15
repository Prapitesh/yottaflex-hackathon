package com.hackathon.api.service;

import com.hackathon.api.dto.response.screening.ScreeningResult;

/**
 * Use cases for evaluating an application against job-specific screening rules.
 */
public interface ScreeningService {

    ScreeningResult screenApplication(String applicationId);
}
