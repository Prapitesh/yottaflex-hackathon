package com.hackathon.api.controller;

import com.hackathon.api.dto.request.interview.*;
import com.hackathon.api.dto.response.interview.InterviewResponse;
import com.hackathon.api.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for interview scheduling and lifecycle operations. */
@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> schedule(
            @Valid @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interviewService.scheduleInterview(request));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponse> getById(
            @PathVariable String interviewId) {
        return ResponseEntity.ok(interviewService.getInterviewById(interviewId));
    }

    @GetMapping("/by-application/{applicationId}")
    public ResponseEntity<List<InterviewResponse>> getForApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
                interviewService.getInterviewsForApplication(applicationId));
    }

    @PostMapping("/{interviewId}/reschedule")
    public ResponseEntity<InterviewResponse> reschedule(
            @PathVariable String interviewId,
            @Valid @RequestBody RescheduleInterviewRequest request) {
        return ResponseEntity.ok(
                interviewService.rescheduleInterview(interviewId, request));
    }

    @PostMapping("/{interviewId}/cancel")
    public ResponseEntity<InterviewResponse> cancel(
            @PathVariable String interviewId,
            @Valid @RequestBody InterviewActionRequest request) {
        return ResponseEntity.ok(
                interviewService.cancelInterview(interviewId, request));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<InterviewResponse> complete(
            @PathVariable String interviewId,
            @Valid @RequestBody InterviewActionRequest request) {
        return ResponseEntity.ok(
                interviewService.completeInterview(interviewId, request));
    }
}
