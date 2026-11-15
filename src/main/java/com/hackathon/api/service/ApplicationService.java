package com.hackathon.api.service;

import com.hackathon.api.dto.request.application.ApplicationDecisionRequest;
import com.hackathon.api.dto.request.application.ApplicationRequest;
import com.hackathon.api.dto.response.application.ApplicationResponse;
import java.util.List;

/** Application use cases; business rules are enforced in the implementation. */
public interface ApplicationService {
    ApplicationResponse createApplication(ApplicationRequest request);
    List<ApplicationResponse> getApplications(String requisitionId, String candidateId);
    ApplicationResponse getApplicationById(String applicationId);
    ApplicationResponse decideApplication(String applicationId, ApplicationDecisionRequest request);
    ApplicationResponse withdrawApplication(String applicationId, String actor, String reason);
}
