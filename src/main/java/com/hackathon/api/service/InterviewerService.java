package com.hackathon.api.service;
import com.hackathon.api.dto.response.interview.InterviewerResponse;
import java.util.List;
public interface InterviewerService {
 List<InterviewerResponse> getActiveInterviewers();
}
