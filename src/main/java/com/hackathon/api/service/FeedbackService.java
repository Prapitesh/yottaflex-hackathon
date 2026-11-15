package com.hackathon.api.service;

import com.hackathon.api.dto.request.feedback.SelectApplicationRequest;
import com.hackathon.api.dto.request.feedback.SubmitFeedbackRequest;
import com.hackathon.api.dto.response.feedback.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    FeedbackResponse submitFeedback(String interviewId, SubmitFeedbackRequest request);
    List<FeedbackResponse> getFeedbackForInterview(String interviewId);
    void selectApplication(String applicationId, SelectApplicationRequest request);
}
