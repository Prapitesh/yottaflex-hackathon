package com.hackathon.api.controller;
import com.hackathon.api.dto.response.interview.InterviewerResponse;
import com.hackathon.api.service.InterviewerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InterviewerControllerTest {
 @Test void getActiveReturnsInterviewerList() {
  InterviewerService service=()->List.of(new InterviewerResponse(
   "INTV-1","Ravi Kumar","ravi@example.test","DEP-1","Asia/Kolkata"));
  var response=new InterviewerController(service).getActive();
  assertEquals(HttpStatus.OK,response.getStatusCode());
  assertEquals("INTV-1",response.getBody().getFirst().id());
 }
}
