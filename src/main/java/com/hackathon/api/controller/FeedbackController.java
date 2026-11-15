package com.hackathon.api.controller;

import com.hackathon.api.dto.request.feedback.SelectApplicationRequest;
import com.hackathon.api.dto.request.feedback.SubmitFeedbackRequest;
import com.hackathon.api.dto.response.feedback.FeedbackResponse;
import com.hackathon.api.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/interviews/{interviewId}/feedback")
    public ResponseEntity<FeedbackResponse> submit(
            @PathVariable String interviewId,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submitFeedback(interviewId, request));
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @GetMapping("/interviews/{interviewId}/feedback")
    public ResponseEntity<List<FeedbackResponse>> getForInterview(
            @PathVariable String interviewId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForInterview(interviewId));
    }

    @PostMapping("/applications/{applicationId}/select")
    public ResponseEntity<Void> select(
            @PathVariable String applicationId,
            @Valid @RequestBody SelectApplicationRequest request) {
        feedbackService.selectApplication(applicationId, request);
        return ResponseEntity.noContent().build();
    }
}
