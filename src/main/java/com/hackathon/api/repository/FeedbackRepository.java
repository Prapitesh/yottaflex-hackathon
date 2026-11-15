package com.hackathon.api.repository;

import com.hackathon.api.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for structured interviewer feedback.
 */
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    List<Feedback> findByInterview_Id(String interviewId);

    Optional<Feedback> findByInterview_IdAndInterviewer_Id(
            String interviewId,
            String interviewerId
    );

    long countByInterview_Id(String interviewId);
}
