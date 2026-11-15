package com.hackathon.api.controller;
import com.hackathon.api.dto.request.offer.*;
import com.hackathon.api.dto.response.offer.OfferResponse;
import com.hackathon.api.enums.OfferStatus;
import com.hackathon.api.service.OfferService;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OfferControllerTest {
 private FakeOfferService service;
 private OfferController controller;
 @BeforeEach void setUp(){service=new FakeOfferService();controller=new OfferController(service);}
 @Test void createReturns201(){var r=controller.create(createRequest());assertEquals(HttpStatus.CREATED,r.getStatusCode());assertEquals(OfferStatus.DRAFT,r.getBody().status());}
 @Test void submitDelegatesOfferId(){var r=controller.submit("OFF-1",actor());assertEquals(HttpStatus.OK,r.getStatusCode());assertEquals("OFF-1",service.lastId);assertTrue(service.submitted);}
 @Test void approveDelegatesToService(){assertEquals(OfferStatus.APPROVED,controller.approve("OFF-1",actor()).getBody().status());assertTrue(service.approved);}
 @Test void issueDelegatesToService(){assertEquals(OfferStatus.ISSUED,controller.issue("OFF-1",actor()).getBody().status());assertTrue(service.issued);}
 @Test void responseDelegatesCandidateDecision(){var req=new OfferResponseRequest(OfferStatus.ACCEPTED,"candidate");assertEquals(OfferStatus.ACCEPTED,controller.respond("OFF-1",req).getBody().status());assertTrue(service.responded);}
 @Test void getReturnsOffer(){assertEquals(HttpStatus.OK,controller.get("OFF-1").getStatusCode());}
 @Test void getForApplicationReturnsList(){assertEquals(1,controller.getForApplication("APP-1").getBody().size());}
 private CreateOfferRequest createRequest(){return new CreateOfferRequest("APP-1",new BigDecimal("900000"),"INR",LocalDate.now().plusDays(5),"recruiter");}
 private OfferActorRequest actor(){return new OfferActorRequest("approver");}
 private static class FakeOfferService implements OfferService {
  String lastId; boolean submitted,approved,issued,responded;
  OfferResponse response(OfferStatus status){return new OfferResponse("OFF-1","APP-1",new BigDecimal("900000"),"INR",status,null,null,null,null,LocalDate.now().plusDays(5),OffsetDateTime.now());}
  public OfferResponse createOffer(CreateOfferRequest r){return response(OfferStatus.DRAFT);}
  public OfferResponse submitForApproval(String id,OfferActorRequest r){lastId=id;submitted=true;return response(OfferStatus.PENDING_APPROVAL);}
  public OfferResponse approveOffer(String id,OfferActorRequest r){approved=true;return response(OfferStatus.APPROVED);}
  public OfferResponse issueOffer(String id,OfferActorRequest r){issued=true;return response(OfferStatus.ISSUED);}
  public OfferResponse recordResponse(String id,OfferResponseRequest r){responded=true;return response(r.response());}
  public OfferResponse getOffer(String id){return response(OfferStatus.DRAFT);}
  public List<OfferResponse> getOffersForApplication(String id){return List.of(response(OfferStatus.DRAFT));}
  public List<OfferResponse> getAllOffers(){return List.of(response(OfferStatus.DRAFT));}
 }
}
