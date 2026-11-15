package com.hackathon.api.service.impl;

import com.hackathon.api.service.JobRequisitionService;

import com.hackathon.api.dto.request.requisition.RequisitionDecisionRequest;
import com.hackathon.api.dto.request.requisition.RequisitionRequest;
import com.hackathon.api.dto.response.requisition.RequisitionResponse;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.DataIssue;
import com.hackathon.api.entity.Department;
import com.hackathon.api.entity.JobRequisition;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.repository.ApplicationRepository;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.DataIssueRepository;
import com.hackathon.api.repository.DepartmentRepository;
import com.hackathon.api.repository.JobRequisitionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implements requisition creation, editing and its controlled approval lifecycle.
 */
@Service
@Transactional
public class JobRequisitionServiceImpl implements JobRequisitionService {

    private static final List<ApplicationStage> ACTIVE_APPLICATION_STAGES = List.of(
            ApplicationStage.APPLIED,
            ApplicationStage.SCREENING,
            ApplicationStage.SHORTLISTED,
            ApplicationStage.INTERVIEW,
            ApplicationStage.SELECTED,
            ApplicationStage.OFFERED
    );

    private final JobRequisitionRepository requisitionRepository;
    private final DepartmentRepository departmentRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditEventRepository auditEventRepository;
    private final DataIssueRepository dataIssueRepository;

    public JobRequisitionServiceImpl(
            JobRequisitionRepository requisitionRepository,
            DepartmentRepository departmentRepository,
            ApplicationRepository applicationRepository,
            AuditEventRepository auditEventRepository,
            DataIssueRepository dataIssueRepository) {
        this.requisitionRepository = requisitionRepository;
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.auditEventRepository = auditEventRepository;
        this.dataIssueRepository = dataIssueRepository;
    }

    @Override
    public RequisitionResponse createRequisition(RequisitionRequest request) {
        Department department = findDepartment(request.departmentId());

        JobRequisition requisition = new JobRequisition(
                EntityIdGenerator.requisitionId(),
                department,
                request.title().trim(),
                request.description().trim(),
                request.employmentType(),
                request.location().trim(),
                request.openings(),
                request.salaryMin(),
                request.salaryMax(),
                request.currency(),
                request.requestedBy().trim()
        );

        JobRequisition saved = requisitionRepository.save(requisition);
        recordAudit(saved, "REQUISITION_CREATED", request.requestedBy(),
                Map.of("status", saved.getStatus().name()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequisitionResponse> getRequisitions(
            RequisitionStatus status, String departmentId) {
        List<JobRequisition> results;

        if (status != null && departmentId != null && !departmentId.isBlank()) {
            results = requisitionRepository.findByStatusAndDepartment_Id(
                    status, departmentId);
        } else if (status != null) {
            results = requisitionRepository.findByStatus(status, Pageable.unpaged())
                    .getContent();
        } else if (departmentId != null && !departmentId.isBlank()) {
            results = requisitionRepository.findByDepartment_Id(departmentId);
        } else {
            results = requisitionRepository.findAll();
        }

        return results.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RequisitionResponse getRequisitionById(String requisitionId) {
        return toResponse(findRequisition(requisitionId));
    }

    @Override
    public RequisitionResponse updateDraft(
            String requisitionId, RequisitionRequest request) {
        JobRequisition requisition = findRequisition(requisitionId);
        requireStatus(requisition, RequisitionStatus.DRAFT,
                "Only draft requisitions can be edited");

        Department department = findDepartment(request.departmentId());
        requisition.updateDetails(
                department,
                request.title().trim(),
                request.description().trim(),
                request.employmentType(),
                request.location().trim(),
                request.openings(),
                request.salaryMin(),
                request.salaryMax()
        );

        JobRequisition saved = requisitionRepository.save(requisition);
        recordAudit(saved, "REQUISITION_DRAFT_UPDATED", request.requestedBy(),
                Map.of("departmentId", department.getId()));
        return toResponse(saved);
    }

    @Override
    public RequisitionResponse submitForApproval(
            String requisitionId, RequisitionDecisionRequest request) {
        JobRequisition requisition = findRequisition(requisitionId);
        requireStatus(requisition, RequisitionStatus.DRAFT,
                "Only draft requisitions can be submitted");

        requisition.submitForApproval();
        JobRequisition saved = requisitionRepository.save(requisition);
        recordAudit(saved, "REQUISITION_SUBMITTED", request.actor(),
                Map.of("reason", request.reason()));
        return toResponse(saved);
    }

    @Override
    public RequisitionResponse approve(
            String requisitionId, RequisitionDecisionRequest request) {
        JobRequisition requisition = findRequisition(requisitionId);
        requireStatus(requisition, RequisitionStatus.PENDING_APPROVAL,
                "Only pending requisitions can be approved");

        if (requisition.getRequestedBy().equalsIgnoreCase(request.actor().trim())) {
            throw new IllegalStateException(
                    "The requester cannot approve their own requisition");
        }

        OffsetDateTime approvedAt = OffsetDateTime.now();
        requisition.approve(request.actor().trim(), approvedAt);
        JobRequisition saved = requisitionRepository.save(requisition);
        recordAudit(saved, "REQUISITION_APPROVED", request.actor(),
                Map.of("reason", request.reason()));
        return toResponse(saved);
    }

    @Override
    public RequisitionResponse reject(
            String requisitionId, RequisitionDecisionRequest request) {
        JobRequisition requisition = findRequisition(requisitionId);
        requireStatus(requisition, RequisitionStatus.PENDING_APPROVAL,
                "Only pending requisitions can be rejected");

        requisition.reject();
        JobRequisition saved = requisitionRepository.save(requisition);
        recordAudit(saved, "REQUISITION_REJECTED", request.actor(),
                Map.of("reason", request.reason()));
        return toResponse(saved);
    }

    @Override
    public RequisitionResponse close(
            String requisitionId, RequisitionDecisionRequest request) {
        JobRequisition requisition = findRequisition(requisitionId);
        requireStatus(requisition, RequisitionStatus.APPROVED,
                "Only approved requisitions can be closed");

        long activeApplications = applicationRepository.countByRequisition_IdAndStageIn(
                requisitionId, ACTIVE_APPLICATION_STAGES);

        OffsetDateTime closedAt = OffsetDateTime.now();
        requisition.close(closedAt);
        JobRequisition saved = requisitionRepository.save(requisition);

        if (activeApplications > 0) {
            dataIssueRepository.save(new DataIssue(
                    "CLOSED_REQUISITION_ACTIVE_APPLICATIONS",
                    "JOB_REQUISITION",
                    requisitionId,
                    "REVIEW_REQUIRED",
                    "Review or resolve active applications after requisition closure.",
                    Map.of("activeApplicationCount", activeApplications)
            ));
        }

        recordAudit(saved, "REQUISITION_CLOSED", request.actor(),
                Map.of("reason", request.reason(),
                        "activeApplicationCount", activeApplications));
        return toResponse(saved);
    }

    private Department findDepartment(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + id));
    }

    private JobRequisition findRequisition(String id) {
        return requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requisition not found: " + id));
    }

    private void requireStatus(
            JobRequisition requisition, RequisitionStatus expected, String message) {
        if (requisition.getStatus() != expected) {
            throw new IllegalStateException(message
                    + "; current status is " + requisition.getStatus());
        }
    }

    private void recordAudit(
            JobRequisition requisition, String eventType, String actor,
            Map<String, Object> payload) {
        auditEventRepository.save(new AuditEvent(
                "JOB_REQUISITION",
                requisition.getId(),
                eventType,
                actor.trim(),
                payload
        ));
    }

    private RequisitionResponse toResponse(JobRequisition requisition) {
        return new RequisitionResponse(
                requisition.getId(),
                requisition.getDepartment().getId(),
                requisition.getTitle(),
                requisition.getDescription(),
                requisition.getEmploymentType(),
                requisition.getLocation(),
                requisition.getOpenings(),
                requisition.getSalaryMin(),
                requisition.getSalaryMax(),
                requisition.getCurrency(),
                requisition.getStatus(),
                requisition.getRequestedBy(),
                requisition.getApprovedBy(),
                requisition.getApprovedAt(),
                requisition.getOpenedAt(),
                requisition.getClosedAt(),
                requisition.getCreatedAt(),
                requisition.getUpdatedAt()
        );
    }
}
