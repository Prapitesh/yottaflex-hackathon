package com.hackathon.api.repository;

import com.hackathon.api.entity.Application;
import com.hackathon.api.enums.ApplicationStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for candidate applications.
 * Duplicate applications remain observable; detection is handled by the service.
 */
public interface ApplicationRepository extends JpaRepository<Application, String> {

    List<Application> findByRequisition_Id(String requisitionId);

    List<Application> findByCandidate_Id(String candidateId);

    List<Application> findByRequisition_IdAndCandidate_Id(
            String requisitionId,
            String candidateId
    );

    List<Application> findByStage(ApplicationStage stage);

    long countByRequisition_IdAndStageIn(
            String requisitionId,
            List<ApplicationStage> stages
    );
}
