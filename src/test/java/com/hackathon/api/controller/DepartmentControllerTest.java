package com.hackathon.api.controller;

import com.hackathon.api.dto.request.DepartmentRequest;
import com.hackathon.api.dto.response.DepartmentResponse;
import com.hackathon.api.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentControllerTest {

    private DepartmentController controller;

    @BeforeEach
    void setUp() {
        controller = new DepartmentController(new FakeDepartmentService());
    }

    @Test
    void createDepartmentReturns201AndLocationHeader() {
        var response = controller.createDepartment(
                new DepartmentRequest("Engineering", "ENG-01")
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/v1/departments/DEPT-000001",
                response.getHeaders().getLocation().toString());
        assertEquals("Engineering", response.getBody().name());
    }

    @Test
    void getAllDepartmentsReturnsDepartmentList() {
        var response = controller.getAllDepartments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Engineering", response.getBody().getFirst().name());
    }

    @Test
    void getDepartmentByIdReturnsRequestedDepartment() {
        var response = controller.getDepartmentById("DEPT-000001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("DEPT-000001", response.getBody().id());
    }

    @Test
    void updateDepartmentReturnsUpdatedDepartment() {
        var response = controller.updateDepartment(
                "DEPT-000001",
                new DepartmentRequest("Platform Engineering", "PLAT-01")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Platform Engineering", response.getBody().name());
    }

    private static class FakeDepartmentService implements DepartmentService {

        private DepartmentResponse department(
                String id, String name, String costCenter) {
            return new DepartmentResponse(
                    id, name, costCenter, OffsetDateTime.parse("2026-11-15T10:00:00+05:30")
            );
        }

        @Override
        public DepartmentResponse createDepartment(DepartmentRequest request) {
            return department("DEPT-000001", request.name(), request.costCenter());
        }

        @Override
        public List<DepartmentResponse> getAllDepartments() {
            return List.of(department("DEPT-000001", "Engineering", "ENG-01"));
        }

        @Override
        public DepartmentResponse getDepartmentById(String departmentId) {
            return department(departmentId, "Engineering", "ENG-01");
        }

        @Override
        public DepartmentResponse updateDepartment(
                String departmentId, DepartmentRequest request) {
            return department(departmentId, request.name(), request.costCenter());
        }
    }
}
