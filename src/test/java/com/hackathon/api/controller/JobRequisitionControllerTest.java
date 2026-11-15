package com.hackathon.api.controller;

import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.dto.request.requisition.RequisitionDecisionRequest;
import com.hackathon.api.dto.request.requisition.RequisitionRequest;
import com.hackathon.api.dto.response.requisition.RequisitionResponse;
import com.hackathon.api.service.JobRequisitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobRequisitionControllerTest {

    private FakeJobRequisitionService service;
    private JobRequisitionController controller;

    @BeforeEach
    void setUp() {
        service = new FakeJobRequisitionService();
        controller = new JobRequisitionController(service);
    }

    @Test
    void createReturns201AndResourceLocation() {
        var response = controller.create(validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/v1/requisitions/REQ-2026-000001",
                response.getHeaders().getLocation().toString());
        assertEquals(RequisitionStatus.DRAFT, response.getBody().status());
    }

    @Test
    void listPassesOptionalFiltersToService() {
        var response = controller.getAll(
                RequisitionStatus.APPROVED, "DEPT-000001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequisitionStatus.APPROVED, service.lastStatus);
        assertEquals("DEPT-000001", service.lastDepartmentId);
    }

    @Test
    void getByIdReturnsRequisition() {
        var response = controller.getById("REQ-2026-000001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Software Engineer", response.getBody().title());
    }

    @Test
    void updateDraftDelegatesToService() {
        var response = controller.updateDraft(
                "REQ-2026-000001", validRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(service.updateCalled);
    }

    @Test
    void submitDelegatesActorAndReason() {
        var response = controller.submit(
                "REQ-2026-000001", decisionRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(service.submitCalled);
    }

    @Test
    void approveDelegatesApprovalDecision() {
        var response = controller.approve(
                "REQ-2026-000001", decisionRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequisitionStatus.APPROVED, response.getBody().status());
    }

    @Test
    void rejectDelegatesRejectionDecision() {
        var response = controller.reject(
                "REQ-2026-000001", decisionRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequisitionStatus.REJECTED, response.getBody().status());
    }

    @Test
    void closeDelegatesCloseDecision() {
        var response = controller.close(
                "REQ-2026-000001", decisionRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(RequisitionStatus.CLOSED, response.getBody().status());
    }

    private RequisitionRequest validRequest() {
        return new RequisitionRequest(
                "DEPT-000001",
                "Software Engineer",
                "Build and maintain recruitment software",
                EmploymentType.FULL_TIME,
                "Hyderabad",
                2,
                new BigDecimal("600000"),
                new BigDecimal("1200000"),
                "INR",
                "requester@example.test"
        );
    }

    private RequisitionDecisionRequest decisionRequest() {
        return new RequisitionDecisionRequest(
                "approver@example.test", "Reviewed against hiring plan"
        );
    }

    private static class FakeJobRequisitionService
            implements JobRequisitionService {

        private RequisitionStatus lastStatus;
        private String lastDepartmentId;
        private boolean updateCalled;
        private boolean submitCalled;

        private RequisitionResponse response(RequisitionStatus status) {
            OffsetDateTime now = OffsetDateTime.parse("2026-11-15T14:00:00+05:30");
            return new RequisitionResponse(
                    "REQ-2026-000001",
                    "DEPT-000001",
                    "Software Engineer",
                    "Build and maintain recruitment software",
                    EmploymentType.FULL_TIME,
                    "Hyderabad",
                    2,
                    new BigDecimal("600000"),
                    new BigDecimal("1200000"),
                    "INR",
                    status,
                    "requester@example.test",
                    status == RequisitionStatus.APPROVED ? "approver@example.test" : null,
                    status == RequisitionStatus.APPROVED ? now : null,
                    status == RequisitionStatus.APPROVED ? now : null,
                    status == RequisitionStatus.CLOSED ? now : null,
                    now,
                    now
            );
        }

        @Override
        public RequisitionResponse createRequisition(RequisitionRequest request) {
            return response(RequisitionStatus.DRAFT);
        }

        @Override
        public List<RequisitionResponse> getRequisitions(
                RequisitionStatus status, String departmentId) {
            lastStatus = status;
            lastDepartmentId = departmentId;
            return List.of(response(status == null ? RequisitionStatus.DRAFT : status));
        }

        @Override
        public RequisitionResponse getRequisitionById(String id) {
            return response(RequisitionStatus.DRAFT);
        }

        @Override
        public RequisitionResponse updateDraft(String id, RequisitionRequest request) {
            updateCalled = true;
            return response(RequisitionStatus.DRAFT);
        }

        @Override
        public RequisitionResponse submitForApproval(
                String id, RequisitionDecisionRequest request) {
            submitCalled = true;
            return response(RequisitionStatus.PENDING_APPROVAL);
        }

        @Override
        public RequisitionResponse approve(
                String id, RequisitionDecisionRequest request) {
            return response(RequisitionStatus.APPROVED);
        }

        @Override
        public RequisitionResponse reject(
                String id, RequisitionDecisionRequest request) {
            return response(RequisitionStatus.REJECTED);
        }

        @Override
        public RequisitionResponse close(
                String id, RequisitionDecisionRequest request) {
            return response(RequisitionStatus.CLOSED);
        }
    }
}

