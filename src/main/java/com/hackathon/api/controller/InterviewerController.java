package com.hackathon.api.controller;
import com.hackathon.api.dto.response.interview.InterviewerResponse;
import com.hackathon.api.service.InterviewerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviewers")
public class InterviewerController {
 private final InterviewerService interviewerService;
 public InterviewerController(InterviewerService interviewerService) {
  this.interviewerService=interviewerService;
 }
 @GetMapping
 public ResponseEntity<List<InterviewerResponse>> getActive() {
  return ResponseEntity.ok(interviewerService.getActiveInterviewers());
 }
}
