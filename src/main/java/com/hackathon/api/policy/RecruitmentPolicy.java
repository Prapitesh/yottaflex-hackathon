package com.hackathon.api.policy;

import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.scheduling.InterviewSlot;
import com.hackathon.api.enums.RequisitionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class RecruitmentPolicy {
    private static final Map<ApplicationStage, EnumSet<ApplicationStage>> TRANSITIONS =
            new EnumMap<>(ApplicationStage.class);

    static {
        TRANSITIONS.put(ApplicationStage.APPLIED, EnumSet.of(ApplicationStage.SCREENING, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.SCREENING, EnumSet.of(ApplicationStage.SHORTLISTED, ApplicationStage.REJECTED, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.SHORTLISTED, EnumSet.of(ApplicationStage.INTERVIEW, ApplicationStage.REJECTED, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.INTERVIEW, EnumSet.of(ApplicationStage.SELECTED, ApplicationStage.REJECTED, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.SELECTED, EnumSet.of(ApplicationStage.OFFERED, ApplicationStage.REJECTED, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.OFFERED, EnumSet.of(ApplicationStage.HIRED, ApplicationStage.REJECTED, ApplicationStage.WITHDRAWN));
        TRANSITIONS.put(ApplicationStage.REJECTED, EnumSet.noneOf(ApplicationStage.class));
        TRANSITIONS.put(ApplicationStage.WITHDRAWN, EnumSet.noneOf(ApplicationStage.class));
        TRANSITIONS.put(ApplicationStage.HIRED, EnumSet.noneOf(ApplicationStage.class));
    }

    private RecruitmentPolicy() { }

    public static boolean canAcceptApplication(RequisitionStatus status) {
        return status == RequisitionStatus.APPROVED;
    }

    public static boolean canTransition(ApplicationStage from, ApplicationStage to) {
        if (from == null || to == null || from == to) return false;
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(ApplicationStage.class)).contains(to);
    }

    public static void requireValidTransition(ApplicationStage from, ApplicationStage to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Invalid application stage transition: " + from + " -> " + to);
        }
    }

    public static boolean isSalaryWithinBand(BigDecimal salary, BigDecimal minimum, BigDecimal maximum) {
        if (salary == null || minimum == null || maximum == null || minimum.compareTo(maximum) > 0) return false;
        return salary.compareTo(minimum) >= 0 && salary.compareTo(maximum) <= 0;
    }

    public static boolean canIssueOffer(ApplicationStage stage, boolean approved,
                                        BigDecimal salary, BigDecimal minimum, BigDecimal maximum) {
        return stage == ApplicationStage.SELECTED && approved
                && isSalaryWithinBand(salary, minimum, maximum);
    }

    public static boolean hasInterviewerConflict(
            String interviewerId, Instant start, Instant end, Collection<InterviewSlot> existingSlots) {
        if (interviewerId == null || start == null || end == null || !end.isAfter(start) || existingSlots == null) {
            throw new IllegalArgumentException("Interviewer, valid time interval and existing slots are required");
        }
        return existingSlots.stream()
                .filter(slot -> interviewerId.equals(slot.interviewerId()))
                .anyMatch(slot -> start.isBefore(slot.end()) && end.isAfter(slot.start()));
    }

    public static boolean hasRequiredFeedback(int submittedFeedback, int requiredFeedback) {
        if (submittedFeedback < 0 || requiredFeedback < 0) {
            throw new IllegalArgumentException("Feedback counts cannot be negative");
        }
        return submittedFeedback >= requiredFeedback;
    }

    public static boolean isDuplicateApplication(long applicationsForCandidateAndRequisition) {
        if (applicationsForCandidateAndRequisition < 0) {
            throw new IllegalArgumentException("Application count cannot be negative");
        }
        return applicationsForCandidateAndRequisition > 1;
    }

    public static boolean hasActiveApplicationsOnClosedRequisition(
            RequisitionStatus status, long activeApplicationCount) {
        if (activeApplicationCount < 0) {
            throw new IllegalArgumentException("Active application count cannot be negative");
        }
        return status == RequisitionStatus.CLOSED && activeApplicationCount > 0;
    }
}

