package com.hackathon.api.policy;

import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.RequisitionStatus;
import com.hackathon.api.scheduling.InterviewSlot;
import com.hackathon.api.policy.RecruitmentPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecruitmentPolicyTest {
    @Test
    void onlyApprovedRequisitionsAcceptApplications() {
        assertTrue(RecruitmentPolicy.canAcceptApplication(RequisitionStatus.APPROVED));
        assertFalse(RecruitmentPolicy.canAcceptApplication(RequisitionStatus.DRAFT));
        assertFalse(RecruitmentPolicy.canAcceptApplication(RequisitionStatus.CLOSED));
    }

    @Test
    void allowsValidApplicationProgression() {
        assertTrue(RecruitmentPolicy.canTransition(ApplicationStage.APPLIED, ApplicationStage.SCREENING));
        assertTrue(RecruitmentPolicy.canTransition(ApplicationStage.INTERVIEW, ApplicationStage.SELECTED));
        assertTrue(RecruitmentPolicy.canTransition(ApplicationStage.SELECTED, ApplicationStage.OFFERED));
    }

    @Test
    void rejectsInvalidOrBackwardStageTransitions() {
        assertFalse(RecruitmentPolicy.canTransition(ApplicationStage.APPLIED, ApplicationStage.HIRED));
        assertFalse(RecruitmentPolicy.canTransition(ApplicationStage.HIRED, ApplicationStage.APPLIED));
        assertFalse(RecruitmentPolicy.canTransition(ApplicationStage.INTERVIEW, ApplicationStage.SCREENING));
        assertThrows(IllegalStateException.class,
                () -> RecruitmentPolicy.requireValidTransition(ApplicationStage.APPLIED, ApplicationStage.HIRED));
    }

    @Test
    void salaryBandIncludesBothBoundariesAndRejectsOutOfBandValues() {
        assertTrue(RecruitmentPolicy.isSalaryWithinBand(new BigDecimal("600000"), new BigDecimal("600000"), new BigDecimal("900000")));
        assertTrue(RecruitmentPolicy.isSalaryWithinBand(new BigDecimal("900000"), new BigDecimal("600000"), new BigDecimal("900000")));
        assertFalse(RecruitmentPolicy.isSalaryWithinBand(new BigDecimal("950000"), new BigDecimal("600000"), new BigDecimal("900000")));
        assertFalse(RecruitmentPolicy.isSalaryWithinBand(new BigDecimal("700000"), new BigDecimal("900000"), new BigDecimal("600000")));
    }

    @Test
    void offerRequiresSelectionApprovalAndSalaryWithinBand() {
        var min = new BigDecimal("600000");
        var max = new BigDecimal("900000");
        assertTrue(RecruitmentPolicy.canIssueOffer(ApplicationStage.SELECTED, true, new BigDecimal("750000"), min, max));
        assertFalse(RecruitmentPolicy.canIssueOffer(ApplicationStage.SELECTED, false, new BigDecimal("750000"), min, max));
        assertFalse(RecruitmentPolicy.canIssueOffer(ApplicationStage.INTERVIEW, true, new BigDecimal("750000"), min, max));
        assertFalse(RecruitmentPolicy.canIssueOffer(ApplicationStage.SELECTED, true, new BigDecimal("950000"), min, max));
    }

    @Test
    void detectsOverlappingInterviewerScheduleButAllowsAdjacentSlots() {
        var existing = List.of(new InterviewSlot("INTVR-1",
                Instant.parse("2026-11-15T09:00:00Z"), Instant.parse("2026-11-15T10:00:00Z")));
        assertTrue(RecruitmentPolicy.hasInterviewerConflict("INTVR-1",
                Instant.parse("2026-11-15T09:30:00Z"), Instant.parse("2026-11-15T10:30:00Z"), existing));
        assertFalse(RecruitmentPolicy.hasInterviewerConflict("INTVR-2",
                Instant.parse("2026-11-15T09:30:00Z"), Instant.parse("2026-11-15T10:30:00Z"), existing));
        assertFalse(RecruitmentPolicy.hasInterviewerConflict("INTVR-1",
                Instant.parse("2026-11-15T10:00:00Z"), Instant.parse("2026-11-15T11:00:00Z"), existing));
    }

    @Test
    void blocksAdvancementWhenRequiredFeedbackIsMissing() {
        assertTrue(RecruitmentPolicy.hasRequiredFeedback(2, 2));
        assertFalse(RecruitmentPolicy.hasRequiredFeedback(1, 2));
    }

    @Test
    void flagsDuplicateApplicationsWithoutDeletingThem() {
        assertFalse(RecruitmentPolicy.isDuplicateApplication(1));
        assertTrue(RecruitmentPolicy.isDuplicateApplication(2));
    }

    @Test
    void detectsActiveApplicationsOnClosedRequisition() {
        assertTrue(RecruitmentPolicy.hasActiveApplicationsOnClosedRequisition(RequisitionStatus.CLOSED, 3));
        assertFalse(RecruitmentPolicy.hasActiveApplicationsOnClosedRequisition(RequisitionStatus.APPROVED, 3));
        assertFalse(RecruitmentPolicy.hasActiveApplicationsOnClosedRequisition(RequisitionStatus.CLOSED, 0));
    }
}


