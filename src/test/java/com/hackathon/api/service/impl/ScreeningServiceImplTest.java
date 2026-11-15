package com.hackathon.api.service.impl;

import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScreeningServiceImplTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ScreeningRuleRepository screeningRuleRepository;
    @Mock private CandidateSkillRepository candidateSkillRepository;
    @Mock private ApplicationStageHistoryRepository stageHistoryRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private ScreeningServiceImpl service;
    private Application application;
    private Candidate candidate;
    private JobRequisition requisition;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ScreeningServiceImpl(
                applicationRepository, screeningRuleRepository,
                candidateSkillRepository, stageHistoryRepository,
                auditEventRepository);

        Department department = new Department("DEP-000001", "Engineering", "ENG-01");
        requisition = new JobRequisition(
                "REQ-2026-000001", department, "Java Developer",
                "Build APIs", EmploymentType.FULL_TIME, "Hyderabad", 1,
                new BigDecimal("600000"), new BigDecimal("1200000"),
                "INR", "requester@example.test");
        requisition.approve("approver@example.test", OffsetDateTime.now());

        candidate = new Candidate("CAND-000001", "Asha Rao",
                "asha@example.test", null, "Hyderabad", 36);
        application = new Application(
                "APP-REQ-2026-000001-CAND-000001",
                requisition, candidate, null);

        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void verifiedRequiredAndPreferredSkillsProducePassAndFullScore() {
        Skill java = new Skill("SKILL-JAVA", "Java", "TECHNICAL");
        Skill spring = new Skill("SKILL-SPRING", "Spring Boot", "TECHNICAL");
        CandidateSkill javaEvidence = new CandidateSkill(
                candidate, java, CandidateSkillProficiency.ADVANCED,
                new BigDecimal("3.0"), SkillEvidenceSource.ASSESSMENT, true);
        CandidateSkill springEvidence = new CandidateSkill(
                candidate, spring, CandidateSkillProficiency.INTERMEDIATE,
                new BigDecimal("2.0"), SkillEvidenceSource.RESUME, true);

        ScreeningRule requiredJava = rule(
                "RULE-1", java, ScreeningRuleType.REQUIRED_SKILL,
                BigDecimal.ZERO, new BigDecimal("2.0"), true);
        ScreeningRule preferredSpring = rule(
                "RULE-2", spring, ScreeningRuleType.PREFERRED_SKILL,
                new BigDecimal("40"), null, false);

        when(screeningRuleRepository
                .findByRequisition_IdOrderByEffectiveFromAsc(requisition.getId()))
                .thenReturn(List.of(requiredJava, preferredSpring));
        when(candidateSkillRepository.findByCandidate_Id(candidate.getId()))
                .thenReturn(List.of(javaEvidence, springEvidence));

        var result = service.screenApplication(application.getId());

        assertEquals(ScreeningDecision.PASS, result.decision());
        assertEquals(new BigDecimal("100.00"), result.score());
        assertEquals(ApplicationStage.SCREENING, result.stage());
        assertTrue(result.reasons().stream().anyMatch(reason -> reason.contains("Java")));
        verify(stageHistoryRepository).save(any(ApplicationStageHistory.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void missingMandatorySkillProducesFail() {
        Skill java = new Skill("SKILL-JAVA", "Java", "TECHNICAL");
        ScreeningRule requiredJava = rule(
                "RULE-1", java, ScreeningRuleType.REQUIRED_SKILL,
                BigDecimal.ZERO, null, true);

        when(screeningRuleRepository
                .findByRequisition_IdOrderByEffectiveFromAsc(requisition.getId()))
                .thenReturn(List.of(requiredJava));
        when(candidateSkillRepository.findByCandidate_Id(candidate.getId()))
                .thenReturn(List.of());

        var result = service.screenApplication(application.getId());

        assertEquals(ScreeningDecision.FAIL, result.decision());
        assertEquals(new BigDecimal("0.00"), result.score());
        assertTrue(result.reasons().stream().anyMatch(reason -> reason.contains("Missing skill")));
    }

    @Test
    void unsupportedRuleProducesReviewInsteadOfAutomaticRejection() {
        ScreeningRule customRule = rule(
                "RULE-CUSTOM", null, ScreeningRuleType.CUSTOM,
                BigDecimal.ZERO, null, true);
        when(screeningRuleRepository
                .findByRequisition_IdOrderByEffectiveFromAsc(requisition.getId()))
                .thenReturn(List.of(customRule));
        when(candidateSkillRepository.findByCandidate_Id(candidate.getId()))
                .thenReturn(List.of());

        var result = service.screenApplication(application.getId());

        assertEquals(ScreeningDecision.REVIEW, result.decision());
        assertTrue(result.reasons().stream()
                .anyMatch(reason -> reason.contains("manual review required")));
    }

    @Test
    void noActiveRulesRequiresReview() {
        when(screeningRuleRepository
                .findByRequisition_IdOrderByEffectiveFromAsc(requisition.getId()))
                .thenReturn(List.of());
        when(candidateSkillRepository.findByCandidate_Id(candidate.getId()))
                .thenReturn(List.of());

        var result = service.screenApplication(application.getId());

        assertEquals(ScreeningDecision.REVIEW, result.decision());
        assertEquals(new BigDecimal("0.00"), result.score());
    }

    @Test
    void applicationCannotBeScreenedTwice() {
        application.changeStage(ApplicationStage.SCREENING, OffsetDateTime.now());

        assertThrows(IllegalStateException.class,
                () -> service.screenApplication(application.getId()));

        verify(applicationRepository, never()).save(any(Application.class));
        verify(stageHistoryRepository, never()).save(any(ApplicationStageHistory.class));
    }

    private ScreeningRule rule(
            String id, Skill skill, ScreeningRuleType type,
            BigDecimal weight, BigDecimal threshold, boolean mandatory) {
        return new ScreeningRule(
                id, requisition, skill, type, weight, threshold, mandatory,
                LocalDate.now().minusDays(1), null);
    }
}
