package com.hackathon.api.service;

import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.dto.request.requisition.RequisitionDecisionRequest;
import com.hackathon.api.dto.request.requisition.RequisitionRequest;
import com.hackathon.api.dto.response.requisition.RequisitionResponse;

import java.util.List;

/**
 * Use-case contract for requisition operations.
 * Implementations will enforce lifecycle, authorization, and audit rules.
 */
public interface JobRequisitionService {

    RequisitionResponse createRequisition(RequisitionRequest request);

    List<RequisitionResponse> getRequisitions(
            RequisitionStatus status, String departmentId);

    RequisitionResponse getRequisitionById(String requisitionId);

    RequisitionResponse updateDraft(
            String requisitionId, RequisitionRequest request);

    RequisitionResponse submitForApproval(
            String requisitionId, RequisitionDecisionRequest request);

    RequisitionResponse approve(
            String requisitionId, RequisitionDecisionRequest request);

    RequisitionResponse reject(
            String requisitionId, RequisitionDecisionRequest request);

    RequisitionResponse close(
            String requisitionId, RequisitionDecisionRequest request);
}

