package com.hackathon.api.repository;

import com.hackathon.api.entity.DataIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for detected and labelled data-quality issues.
 */
public interface DataIssueRepository extends JpaRepository<DataIssue, Long> {

    List<DataIssue> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<DataIssue> findByIssueType(String issueType);

    List<DataIssue> findByResolvedAtIsNullOrderByDetectedAtDesc();
}
