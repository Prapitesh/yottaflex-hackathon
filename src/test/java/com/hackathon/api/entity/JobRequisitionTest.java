package com.hackathon.api.entity;

import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JobRequisitionTest {

    private JobRequisition newRequisition() {
        Department department = new Department("DEPT-001", "Engineering", "CC-100");
        return new JobRequisition(
                "REQ-2026-000001",
                department,
                "Java Backend Engineer",
                "Build and maintain recruitment platform services.",
                EmploymentType.FULL_TIME,
                "Hyderabad",
                2,
                new BigDecimal("600000.00"),
                new BigDecimal("1200000.00"),
                "INR",
                "business.requester"
        );
    }

    @Test
    void newRequisitionStartsAsDraftAndReferencesItsDepartment() {
        JobRequisition requisition = newRequisition();

        assertEquals(RequisitionStatus.DRAFT, requisition.getStatus());
        assertEquals("DEPT-001", requisition.getDepartment().getId());
        assertEquals("INR", requisition.getCurrency());
    }

    @Test
    void approvalSetsApprovalAndOpeningMetadata() {
        JobRequisition requisition = newRequisition();
        OffsetDateTime approvalTime = OffsetDateTime.now();

        requisition.submitForApproval();
        requisition.approve("department.head", approvalTime);

        assertEquals(RequisitionStatus.APPROVED, requisition.getStatus());
        assertEquals("department.head", requisition.getApprovedBy());
        assertEquals(approvalTime, requisition.getApprovedAt());
        assertEquals(approvalTime, requisition.getOpenedAt());
        assertTrue(requisition.isApprovalMetadataValid());
    }

    @Test
    void rejectionDoesNotPolluteApprovalMetadata() {
        JobRequisition requisition = newRequisition();

        requisition.submitForApproval();
        requisition.reject();

        assertEquals(RequisitionStatus.REJECTED, requisition.getStatus());
        assertNull(requisition.getApprovedBy());
        assertNull(requisition.getApprovedAt());
    }

    @Test
    void salaryRangeValidationRejectsMaximumBelowMinimum() {
        JobRequisition requisition = newRequisition();
        requisition.updateDetails(
                "Java Backend Engineer",
                "Updated role description",
                EmploymentType.FULL_TIME,
                "Hyderabad",
                2,
                new BigDecimal("1200000.00"),
                new BigDecimal("600000.00")
        );

        assertFalse(requisition.isSalaryRangeValid());
    }

    @Test
    void lifecycleCallbacksMaintainCreationAndUpdateTimestamps() {
        JobRequisition requisition = newRequisition();

        requisition.onCreate();
        OffsetDateTime createdAt = requisition.getCreatedAt();

        assertNotNull(createdAt);
        assertNotNull(requisition.getUpdatedAt());

        requisition.onUpdate();

        assertNotNull(requisition.getUpdatedAt());
        assertFalse(requisition.getUpdatedAt().isBefore(createdAt));
    }
}
