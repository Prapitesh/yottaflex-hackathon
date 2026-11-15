package com.hackathon.api.repository;

import com.hackathon.api.entity.Interviewer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewerRepository extends JpaRepository<Interviewer, String> {

    Optional<Interviewer> findByEmailIgnoreCase(String email);

    List<Interviewer> findByDepartment_Id(String departmentId);

    List<Interviewer> findByActiveTrue();

    /**
     * Serializes schedule checks for a given interviewer while the service
     * transaction is active, reducing the risk of concurrent double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select interviewer from Interviewer interviewer where interviewer.id = :id")
    Optional<Interviewer> findByIdForScheduling(@Param("id") String id);
}
