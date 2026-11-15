package com.hackathon.api.controller;
import com.hackathon.api.dto.request.interview.*;
import com.hackathon.api.dto.response.interview.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class InterviewControllerTest {
 @Test void scheduleReturnsCreated(){
  InterviewController c=new InterviewController(new FakeService());
  assertEquals(HttpStatus.CREATED,c.schedule(null).getStatusCode());
 }
 @Test void getForApplicationReturnsRecords(){
  InterviewController c=new InterviewController(new FakeService());
  assertEquals(1,c.getForApplication("APP-1").getBody().size());
 }
 @Test void getAllReturnsAllInterviewRecords(){
  InterviewController c=new InterviewController(new FakeService());
  assertEquals(1,c.getAll().getBody().size());
 }
 @Test void rescheduleCancelAndCompleteDelegate(){
  FakeService f=new FakeService(); InterviewController c=new InterviewController(f);
  c.reschedule("INT-1",null); c.cancel("INT-1",null); c.complete("INT-1",null);
  assertTrue(f.rescheduled&&f.cancelled&&f.completed);
 }
 private static class FakeService implements InterviewService {
  boolean rescheduled,cancelled,completed;
  InterviewResponse response(){return new InterviewResponse("INT-1","APP-1",InterviewType.TECHNICAL,OffsetDateTime.now(),OffsetDateTime.now().plusHours(1),InterviewMode.ONLINE,null,InterviewStatus.SCHEDULED,List.of());}
  public InterviewResponse scheduleInterview(ScheduleInterviewRequest r){return response();}
  public InterviewResponse getInterviewById(String id){return response();}
  public List<InterviewResponse> getAllInterviews(){return List.of(response());}
  public List<InterviewResponse> getInterviewsForApplication(String id){return List.of(response());}
  public InterviewResponse rescheduleInterview(String id,RescheduleInterviewRequest r){rescheduled=true;return response();}
  public InterviewResponse cancelInterview(String id,InterviewActionRequest r){cancelled=true;return response();}
  public InterviewResponse completeInterview(String id,InterviewActionRequest r){completed=true;return response();}
 }
}