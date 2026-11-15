package com.hackathon.api.repository;

import com.hackathon.api.entity.InterviewInterviewer;
import com.hackathon.api.entity.InterviewInterviewerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for interview panel assignments.
 */
public interface InterviewInterviewerRepository
        extends JpaRepository<InterviewInterviewer, InterviewInterviewerId> {

    List<InterviewInterviewer> findByInterview_Id(String interviewId);

    List<InterviewInterviewer> findByInterviewer_Id(String interviewerId);

    boolean existsByInterview_IdAndInterviewer_Id(
            String interviewId,
            String interviewerId
    );
}
