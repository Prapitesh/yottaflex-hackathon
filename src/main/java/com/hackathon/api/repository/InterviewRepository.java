package com.hackathon.api.repository;

import com.hackathon.api.entity.Interview;
import com.hackathon.api.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, String> {

    List<Interview> findByApplication_IdOrderByScheduledStartDesc(String applicationId);

    List<Interview> findAllByOrderByScheduledStartDesc();

    @Query("""
            select distinct interview
            from Interview interview
            join InterviewInterviewer assignment
                on assignment.interview = interview
            where assignment.interviewer.id = :interviewerId
              and interview.status in :activeStatuses
              and interview.scheduledStart < :requestedEnd
              and interview.scheduledEnd > :requestedStart
              and (:excludedInterviewId is null or interview.id <> :excludedInterviewId)
            """)
    List<Interview> findConflictingInterviewsForInterviewer(
            @Param("interviewerId") String interviewerId,
            @Param("activeStatuses") List<InterviewStatus> activeStatuses,
            @Param("requestedStart") OffsetDateTime requestedStart,
            @Param("requestedEnd") OffsetDateTime requestedEnd,
            @Param("excludedInterviewId") String excludedInterviewId
    );
}
