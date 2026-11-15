package com.hackathon.api.service;

import com.hackathon.api.dto.request.interview.*;
import com.hackathon.api.dto.response.interview.InterviewResponse;
import java.util.List;

public interface InterviewService {
    InterviewResponse scheduleInterview(ScheduleInterviewRequest request);
    InterviewResponse getInterviewById(String interviewId);
    List<InterviewResponse> getInterviewsForApplication(String applicationId);
    InterviewResponse rescheduleInterview(String interviewId, RescheduleInterviewRequest request);
    InterviewResponse cancelInterview(String interviewId, InterviewActionRequest request);
    InterviewResponse completeInterview(String interviewId, InterviewActionRequest request);
}
