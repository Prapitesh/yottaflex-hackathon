package com.hackathon.api.controller;
import com.hackathon.api.dto.request.feedback.*;
import com.hackathon.api.dto.response.feedback.FeedbackResponse;
import com.hackathon.api.enums.FeedbackRecommendation;
import com.hackathon.api.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class FeedbackControllerTest {
 @Test void submitReturnsCreated(){FeedbackController c=new FeedbackController(new FakeService());assertEquals(HttpStatus.CREATED,c.submit("INT-1",null).getStatusCode());}
 @Test void getReturnsFeedback(){FeedbackController c=new FeedbackController(new FakeService());assertEquals(1,c.getForInterview("INT-1").getBody().size());}
 @Test void selectReturnsNoContent(){FakeService f=new FakeService();FeedbackController c=new FeedbackController(f);assertEquals(HttpStatus.NO_CONTENT,c.select("APP-1",null).getStatusCode());assertTrue(f.selected);}
 private static class FakeService implements FeedbackService {
  boolean selected;
  FeedbackResponse response(){return new FeedbackResponse("FB-1","INT-1","INTV-1","Interviewer",new BigDecimal("8.5"),FeedbackRecommendation.YES,"Clear problem solving",null,OffsetDateTime.now());}
  public FeedbackResponse submitFeedback(String id,SubmitFeedbackRequest r){return response();}
  public List<FeedbackResponse> getFeedbackForInterview(String id){return List.of(response());}
  public void selectApplication(String id,SelectApplicationRequest r){selected=true;}
 }
}