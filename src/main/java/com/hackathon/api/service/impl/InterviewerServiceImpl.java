package com.hackathon.api.service.impl;
import com.hackathon.api.dto.response.interview.InterviewerResponse;
import com.hackathon.api.repository.InterviewerRepository;
import com.hackathon.api.service.InterviewerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InterviewerServiceImpl implements InterviewerService {
 private final InterviewerRepository interviewerRepository;
 public InterviewerServiceImpl(InterviewerRepository interviewerRepository) {
  this.interviewerRepository=interviewerRepository;
 }
 @Override
 public List<InterviewerResponse> getActiveInterviewers() {
  return interviewerRepository.findByActiveTrue().stream()
   .map(person -> new InterviewerResponse(person.getId(),person.getFullName(),
    person.getEmail(),person.getDepartment()==null?null:person.getDepartment().getId(),
    person.getTimezone()))
   .toList();
 }
}
