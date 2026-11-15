package com.hackathon.api.repository;

import com.hackathon.api.entity.ScreeningRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for requisition-specific screening criteria.
 */
public interface ScreeningRuleRepository extends JpaRepository<ScreeningRule, String> {

    List<ScreeningRule> findByRequisition_IdOrderByEffectiveFromAsc(String requisitionId);
}
