package com.hackathon.api.service.impl;

import com.hackathon.api.dto.response.screening.ScreeningResult;
import com.hackathon.api.entity.Application;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.CandidateSkill;
import com.hackathon.api.entity.ScreeningRule;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.ScreeningDecision;
import com.hackathon.api.enums.ScreeningRuleType;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.ApplicationRepository;
import com.hackathon.api.repository.ApplicationStageHistoryRepository;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.CandidateSkillRepository;
import com.hackathon.api.repository.ScreeningRuleRepository;
import com.hackathon.api.policy.RecruitmentPolicy;
import com.hackathon.api.service.ScreeningService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates only criteria configured for the specific requisition.
 *
 * Mandatory criteria act as gates. Non-mandatory skill and experience criteria
 * contribute their configured weights to a normalized 0-100 score.
 * Unsupported rule types and unverified evidence produce REVIEW, not an
 * unsupported automatic rejection.
 */
@Service
@Transactional
public class ScreeningServiceImpl implements ScreeningService {

    private final ApplicationRepository applicationRepository;
    private final ScreeningRuleRepository screeningRuleRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final ApplicationStageHistoryRepository stageHistoryRepository;
    private final AuditEventRepository auditEventRepository;

    public ScreeningServiceImpl(
            ApplicationRepository applicationRepository,
            ScreeningRuleRepository screeningRuleRepository,
            CandidateSkillRepository candidateSkillRepository,
            ApplicationStageHistoryRepository stageHistoryRepository,
            AuditEventRepository auditEventRepository) {
        this.applicationRepository = applicationRepository;
        this.screeningRuleRepository = screeningRuleRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.stageHistoryRepository = stageHistoryRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public ScreeningResult screenApplication(String applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found: " + applicationId));

        if (application.getStage() != ApplicationStage.APPLIED) {
            throw new IllegalStateException(
                    "Only APPLIED applications can be screened; current stage is "
                            + application.getStage());
        }

        LocalDate today = LocalDate.now();
        List<ScreeningRule> activeRules = screeningRuleRepository
                .findByRequisition_IdOrderByEffectiveFromAsc(
                        application.getRequisition().getId())
                .stream()
                .filter(rule -> isActive(rule, today))
                .toList();

        List<CandidateSkill> candidateSkills =
                candidateSkillRepository.findByCandidate_Id(
                        application.getCandidate().getId());
        Map<String, CandidateSkill> skillsById = new HashMap<>();
        for (CandidateSkill candidateSkill : candidateSkills) {
            skillsById.put(candidateSkill.getSkill().getId(), candidateSkill);
        }

        Evaluation evaluation = evaluate(activeRules, application, skillsById);
        RecruitmentPolicy.requireValidTransition(
                application.getStage(), ApplicationStage.SCREENING);

        OffsetDateTime changedAt = OffsetDateTime.now();
        application.recordScreening(evaluation.score(), evaluation.decision());
        application.changeStage(ApplicationStage.SCREENING, changedAt);
        Application saved = applicationRepository.save(application);

        String reason = "Automated screening: " + evaluation.decision()
                + "; score=" + evaluation.score();
        stageHistoryRepository.save(new com.hackathon.api.entity.ApplicationStageHistory(
                saved, ApplicationStage.APPLIED, ApplicationStage.SCREENING,
                "SYSTEM", reason));

        auditEventRepository.save(new AuditEvent(
                "APPLICATION",
                saved.getId(),
                "APPLICATION_SCREENED",
                "SYSTEM",
                Map.of(
                        "score", evaluation.score(),
                        "decision", evaluation.decision().name(),
                        "activeRuleCount", activeRules.size(),
                        "reasons", evaluation.reasons()
                )
        ));

        return new ScreeningResult(
                saved.getId(),
                evaluation.score(),
                evaluation.decision(),
                saved.getStage(),
                evaluation.reasons()
        );
    }

    private Evaluation evaluate(
            List<ScreeningRule> rules,
            Application application,
            Map<String, CandidateSkill> candidateSkills) {

        List<String> reasons = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal achievedWeight = BigDecimal.ZERO;
        boolean mandatoryFailure = false;
        boolean requiresReview = false;

        if (rules.isEmpty()) {
            reasons.add("No active screening rules are configured for this requisition.");
            return new Evaluation(
                    BigDecimal.ZERO.setScale(2), ScreeningDecision.REVIEW, reasons);
        }

        for (ScreeningRule rule : rules) {
            ScreeningRuleType type = rule.getRuleType();

            if (type == ScreeningRuleType.REQUIRED_SKILL
                    || type == ScreeningRuleType.PREFERRED_SKILL) {
                CandidateSkill evidence = rule.getSkill() == null
                        ? null : candidateSkills.get(rule.getSkill().getId());

                boolean mandatory = rule.isMandatory();
                boolean weighted = !mandatory;
                if (weighted && positive(rule.getWeight())) {
                    totalWeight = totalWeight.add(rule.getWeight());
                }

                if (evidence == null) {
                    reasons.add("Missing skill: "
                            + (rule.getSkill() == null ? "unconfigured skill"
                            : rule.getSkill().getName()));
                    if (mandatory) {
                        mandatoryFailure = true;
                    }
                    continue;
                }

                if (!evidence.isVerified()) {
                    reasons.add("Skill evidence requires verification: "
                            + rule.getSkill().getName());
                    requiresReview = true;
                    continue;
                }

                if (belowThreshold(evidence.getYearsExperience(), rule.getThreshold())) {
                    reasons.add("Insufficient recorded experience for skill: "
                            + rule.getSkill().getName());
                    if (mandatory) {
                        mandatoryFailure = true;
                    }
                    continue;
                }

                reasons.add("Matched skill criterion: " + rule.getSkill().getName());
                if (weighted && positive(rule.getWeight())) {
                    achievedWeight = achievedWeight.add(rule.getWeight());
                }
                continue;
            }

            if (type == ScreeningRuleType.MIN_EXPERIENCE) {
                if (rule.getThreshold() == null) {
                    reasons.add("Minimum experience rule has no threshold.");
                    requiresReview = true;
                    continue;
                }

                BigDecimal candidateYears = BigDecimal.valueOf(
                        application.getCandidate().getTotalExperienceMonths())
                        .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                boolean meets = candidateYears.compareTo(rule.getThreshold()) >= 0;

                if (rule.isMandatory()) {
                    if (!meets) {
                        mandatoryFailure = true;
                        reasons.add("Does not meet mandatory minimum experience of "
                                + rule.getThreshold() + " years.");
                    } else {
                        reasons.add("Meets mandatory minimum experience.");
                    }
                } else if (positive(rule.getWeight())) {
                    totalWeight = totalWeight.add(rule.getWeight());
                    if (meets) {
                        achievedWeight = achievedWeight.add(rule.getWeight());
                        reasons.add("Meets preferred minimum experience.");
                    } else {
                        reasons.add("Does not meet preferred minimum experience.");
                    }
                }
                continue;
            }

            // LOCATION and CUSTOM lack a configured comparison value in the
            // current data model. Do not guess or silently treat them as passes.
            reasons.add("Rule " + rule.getId() + " (" + type
                    + ") needs a supported comparison value; manual review required.");
            requiresReview = true;
        }

        BigDecimal score;
        if (totalWeight.signum() > 0) {
            score = achievedWeight.multiply(BigDecimal.valueOf(100))
                    .divide(totalWeight, 2, RoundingMode.HALF_UP);
        } else {
            score = mandatoryFailure || requiresReview
                    ? BigDecimal.ZERO.setScale(2)
                    : BigDecimal.valueOf(100).setScale(2);
        }

        ScreeningDecision decision = mandatoryFailure
                ? ScreeningDecision.FAIL
                : requiresReview ? ScreeningDecision.REVIEW : ScreeningDecision.PASS;

        if (reasons.isEmpty()) {
            reasons.add("All configured screening criteria were satisfied.");
        }
        return new Evaluation(score, decision, reasons);
    }

    private boolean isActive(ScreeningRule rule, LocalDate today) {
        return !rule.getEffectiveFrom().isAfter(today)
                && (rule.getEffectiveTo() == null
                || !rule.getEffectiveTo().isBefore(today));
    }

    private boolean belowThreshold(BigDecimal actual, BigDecimal threshold) {
        return threshold != null && actual.compareTo(threshold) < 0;
    }

    private boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private record Evaluation(
            BigDecimal score,
            ScreeningDecision decision,
            List<String> reasons) {
    }
}
