package com.hackathon.api.controller;

import com.hackathon.api.dto.request.application.*;
import com.hackathon.api.dto.response.application.ApplicationResponse;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerTest {
    private FakeApplicationService service;
    private ApplicationController controller;

    @BeforeEach void setUp() {
        service = new FakeApplicationService();
        controller = new ApplicationController(service);
    }

    @Test void createReturns201AndLocation() {
        var response = controller.create(validRequest());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/v1/applications/APP-REQ-000001-CAND-000001",
                response.getHeaders().getLocation().toString());
        assertEquals(ApplicationStage.APPLIED, response.getBody().stage());
    }

    @Test void getAllPassesFiltersToService() {
        var response = controller.getAll("REQ-000001", "CAND-000001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("REQ-000001", service.lastRequisitionId);
        assertEquals("CAND-000001", service.lastCandidateId);
    }

    @Test void getByIdReturnsApplication() {
        var response = controller.getById("APP-REQ-000001-CAND-000001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CAND-000001", response.getBody().candidateId());
    }

    @Test void decisionPassesTargetStageAndReturnsUpdatedApplication() {
        var response = controller.decide("APP-REQ-000001-CAND-000001",
                new ApplicationDecisionRequest(ApplicationStage.SHORTLISTED,
                        "recruiter@example.test", "Passed screening"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ApplicationStage.SHORTLISTED, response.getBody().stage());
        assertTrue(service.decisionCalled);
    }

    @Test void withdrawPassesActorAndReasonToService() {
        var response = controller.withdraw("APP-REQ-000001-CAND-000001",
                new WithdrawApplicationRequest("candidate@example.test",
                        "Accepted another opportunity"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ApplicationStage.WITHDRAWN, response.getBody().stage());
        assertTrue(service.withdrawCalled);
    }

    private ApplicationRequest validRequest() {
        return new ApplicationRequest("REQ-000001", "CAND-000001", null);
    }

    private static class FakeApplicationService implements ApplicationService {
        private String lastRequisitionId;
        private String lastCandidateId;
        private boolean withdrawCalled;
        private boolean decisionCalled;

        private ApplicationResponse response(ApplicationStage stage) {
            OffsetDateTime now = OffsetDateTime.parse("2026-11-15T15:00:00+05:30");
            return new ApplicationResponse("APP-REQ-000001-CAND-000001",
                    "REQ-000001", "CAND-000001", null, stage, now, now);
        }

        @Override public ApplicationResponse createApplication(ApplicationRequest request) {
            return response(ApplicationStage.APPLIED);
        }
        @Override public List<ApplicationResponse> getApplications(String requisitionId, String candidateId) {
            lastRequisitionId = requisitionId;
            lastCandidateId = candidateId;
            return List.of(response(ApplicationStage.APPLIED));
        }
        @Override public ApplicationResponse getApplicationById(String applicationId) {
            return response(ApplicationStage.APPLIED);
        }
        @Override public ApplicationResponse decideApplication(String applicationId, ApplicationDecisionRequest request) {
            decisionCalled = true;
            return response(request.targetStage());
        }
        @Override public ApplicationResponse withdrawApplication(String applicationId, String actor, String reason) {
            withdrawCalled = true;
            return response(ApplicationStage.WITHDRAWN);
        }
    }
}
