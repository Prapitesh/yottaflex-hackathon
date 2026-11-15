package com.hackathon.api.service.impl;
import com.hackathon.api.entity.*;
import com.hackathon.api.repository.InterviewerRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewerServiceImplTest {
 @Test void returnsOnlyActiveInterviewersFromRepository() {
  InterviewerRepository repository=mock(InterviewerRepository.class);
  Department department=new Department("DEP-1","Engineering","ENG");
  Interviewer active=new Interviewer("INTV-1","Ravi Kumar","ravi@example.test",department,"Asia/Kolkata");
  when(repository.findByActiveTrue()).thenReturn(List.of(active));
  var result=new InterviewerServiceImpl(repository).getActiveInterviewers();
  assertEquals(1,result.size());
  assertEquals("INTV-1",result.getFirst().id());
  assertEquals("DEP-1",result.getFirst().departmentId());
 }
}
