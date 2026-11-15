package com.hackathon.api.controller;

import com.hackathon.api.dto.request.candidate.CandidateRequest;
import com.hackathon.api.dto.response.candidate.CandidateResponse;
import com.hackathon.api.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandidateControllerTest {

    private FakeCandidateService service;
    private CandidateController controller;

    @BeforeEach
    void setUp() {
        service = new FakeCandidateService();
        controller = new CandidateController(service);
    }

    @Test
    void createReturns201AndLocation() {
        var response = controller.create(validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/v1/candidates/CAND-000001",
                response.getHeaders().getLocation().toString());
        assertEquals("Aarav Sharma", response.getBody().fullName());
    }

    @Test
    void getAllReturnsCandidateProfiles() {
        var response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getByIdReturnsCandidate() {
        var response = controller.getById("CAND-000001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CAND-000001", response.getBody().id());
    }

    @Test
    void updateDelegatesToService() {
        var response = controller.update("CAND-000001", validRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(service.updateCalled);
        assertEquals("Aarav Sharma", response.getBody().fullName());
    }

    private CandidateRequest validRequest() {
        return new CandidateRequest(
                "Aarav Sharma",
                "aarav@example.test",
                "+91-9000000000",
                "Hyderabad",
                18
        );
    }

    private static class FakeCandidateService implements CandidateService {

        private boolean updateCalled;

        private CandidateResponse response(
                String id, String name, String email, Integer experience) {
            return new CandidateResponse(
                    id, name, email, "+91-9000000000", "Hyderabad", experience,
                    OffsetDateTime.parse("2026-11-15T14:30:00+05:30")
            );
        }

        @Override
        public CandidateResponse createCandidate(CandidateRequest request) {
            return response("CAND-000001", request.fullName(),
                    request.email(), request.totalExperienceMonths());
        }

        @Override
        public List<CandidateResponse> getAllCandidates() {
            return List.of(response(
                    "CAND-000001", "Aarav Sharma",
                    "aarav@example.test", 18
            ));
        }

        @Override
        public CandidateResponse getCandidateById(String candidateId) {
            return response(candidateId, "Aarav Sharma",
                    "aarav@example.test", 18);
        }

        @Override
        public CandidateResponse updateCandidate(
                String candidateId, CandidateRequest request) {
            updateCalled = true;
            return response(candidateId, request.fullName(),
                    request.email(), request.totalExperienceMonths());
        }
    }
}
