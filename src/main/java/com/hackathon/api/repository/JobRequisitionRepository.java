package com.hackathon.api.repository;

import com.hackathon.api.entity.JobRequisition;
import com.hackathon.api.enums.RequisitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for job requisitions.
 * Business decisions and lifecycle rules belong in the service layer.
 */
public interface JobRequisitionRepository extends JpaRepository<JobRequisition, String> {

    List<JobRequisition> findByDepartment_Id(String departmentId);

    Page<JobRequisition> findByStatus(RequisitionStatus status, Pageable pageable);

    List<JobRequisition> findByStatusAndDepartment_Id(
            RequisitionStatus status,
            String departmentId
    );
}
