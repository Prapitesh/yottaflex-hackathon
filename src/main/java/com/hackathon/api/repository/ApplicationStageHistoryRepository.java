package com.hackathon.api.repository;

import com.hackathon.api.entity.ApplicationStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Read access to the append-only history of application stage transitions.
 */
public interface ApplicationStageHistoryRepository
        extends JpaRepository<ApplicationStageHistory, Long> {

    List<ApplicationStageHistory> findByApplication_IdOrderByChangedAtAsc(
            String applicationId
    );
}
