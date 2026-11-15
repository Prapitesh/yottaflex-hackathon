package com.hackathon.api.service;

import com.hackathon.api.dto.request.DepartmentRequest;
import com.hackathon.api.dto.response.DepartmentResponse;

import java.util.List;

/**
 * Application-layer contract used by the DepartmentController.
 * Business rules and persistence orchestration will be implemented here.
 */
public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentRequest request);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(String departmentId);

    DepartmentResponse updateDepartment(String departmentId, DepartmentRequest request);
}
