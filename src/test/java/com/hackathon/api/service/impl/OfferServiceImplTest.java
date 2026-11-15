package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.offer.*;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OfferServiceImplTest {
 @Mock private OfferRepository offers;
 @Mock private ApplicationRepository applications;
 @Mock private ApplicationStageHistoryRepository history;
 @Mock private AuditEventRepository audit;
 private OfferServiceImpl service;
 private Application app;
 private Offer offer;

 @BeforeEach void setUp() {
  MockitoAnnotations.openMocks(this);
  service=new OfferServiceImpl(offers,applications,history,audit);
  Department dep=new Department("DEP-1","Engineering","ENG");
  JobRequisition req=new JobRequisition("REQ-2026-1",dep,"Java Engineer","Build",
   EmploymentType.FULL_TIME,"Hyderabad",1,new BigDecimal("600000"),
   new BigDecimal("1200000"),"INR","requester");
  req.approve("approver",OffsetDateTime.now().minusDays(1));
  Candidate candidate=new Candidate("CAND-1","Asha Rao","asha@example.test",null,"Hyderabad",12);
  app=new Application("APP-REQ-2026-1-CAND-1",req,candidate,null);
  app.changeStage(ApplicationStage.SELECTED,OffsetDateTime.now());
  offer=new Offer("OFF-2026-ABC12345",app,new BigDecimal("900000"),"INR",LocalDate.now().plusDays(10));
  when(applications.findById(app.getId())).thenReturn(Optional.of(app));
  when(offers.findById(offer.getId())).thenReturn(Optional.of(offer));
  when(offers.findByApplication_IdOrderByCreatedAtDesc(app.getId())).thenReturn(new ArrayList<>());
  when(offers.save(any(Offer.class))).thenAnswer(i->i.getArgument(0));
  when(applications.save(any(Application.class))).thenAnswer(i->i.getArgument(0));
 }
 private CreateOfferRequest createRequest() {
  return new CreateOfferRequest(app.getId(),new BigDecimal("900000"),"INR",LocalDate.now().plusDays(10),"recruiter");
 }
 private OfferActorRequest actor() { return new OfferActorRequest("approver"); }

 @Test void createsDraftOfferWithinApprovedSalaryBand() {
  var result=service.createOffer(createRequest());
  assertEquals(OfferStatus.DRAFT,result.status());
  assertEquals("INR",result.currency());
  verify(offers).save(any(Offer.class)); verify(audit).save(any(AuditEvent.class));
 }
 @Test void rejectsOfferForApplicationNotSelected() {
  app.changeStage(ApplicationStage.INTERVIEW,OffsetDateTime.now());
  assertThrows(IllegalStateException.class,()->service.createOffer(createRequest()));
  verify(offers,never()).save(any());
 }
 @Test void rejectsSalaryOutsideApprovedBand() {
  var request=new CreateOfferRequest(app.getId(),new BigDecimal("1500000"),"INR",LocalDate.now().plusDays(10),"recruiter");
  assertThrows(IllegalArgumentException.class,()->service.createOffer(request));
 }
 @Test void rejectsCurrencyDifferentFromRequisition() {
  var request=new CreateOfferRequest(app.getId(),new BigDecimal("900000"),"USD",LocalDate.now().plusDays(10),"recruiter");
  assertThrows(IllegalArgumentException.class,()->service.createOffer(request));
 }
 @Test void preventsSecondActiveOffer() {
  when(offers.findByApplication_IdOrderByCreatedAtDesc(app.getId())).thenReturn(List.of(offer));
  assertThrows(IllegalStateException.class,()->service.createOffer(createRequest()));
 }
 @Test void enforcesDraftThenPendingThenApprovedLifecycle() {
  assertThrows(IllegalStateException.class,()->service.approveOffer(offer.getId(),actor()));
  assertEquals(OfferStatus.PENDING_APPROVAL,service.submitForApproval(offer.getId(),actor()).status());
  assertEquals(OfferStatus.APPROVED,service.approveOffer(offer.getId(),actor()).status());
 }
 @Test void issueMovesApplicationToOfferedAndRecordsHistory() {
  offer.submitForApproval(); offer.approve("approver",OffsetDateTime.now().minusMinutes(1));
  assertEquals(OfferStatus.ISSUED,service.issueOffer(offer.getId(),actor()).status());
  assertEquals(ApplicationStage.OFFERED,app.getStage());
  verify(history).save(any(ApplicationStageHistory.class));
  verify(applications).save(app);
 }
 @Test void approvalIsRequiredBeforeIssue() {
  assertThrows(IllegalStateException.class,()->service.issueOffer(offer.getId(),actor()));
  verify(history,never()).save(any());
 }
 @Test void acceptingOfferMovesApplicationToHired() {
  offer.submitForApproval(); offer.approve("approver",OffsetDateTime.now().minusMinutes(2));
  offer.issue(OffsetDateTime.now().minusMinutes(1)); app.changeStage(ApplicationStage.OFFERED,OffsetDateTime.now());
  var result=service.recordResponse(offer.getId(),new OfferResponseRequest(OfferStatus.ACCEPTED,"candidate"));
  assertEquals(OfferStatus.ACCEPTED,result.status());
  assertEquals(ApplicationStage.HIRED,app.getStage());
  verify(history).save(any(ApplicationStageHistory.class));
 }
 @Test void rejectingOfferMovesApplicationToRejected() {
  offer.submitForApproval(); offer.approve("approver",OffsetDateTime.now().minusMinutes(2));
  offer.issue(OffsetDateTime.now().minusMinutes(1)); app.changeStage(ApplicationStage.OFFERED,OffsetDateTime.now());
  var result=service.recordResponse(offer.getId(),new OfferResponseRequest(OfferStatus.REJECTED,"candidate"));
  assertEquals(OfferStatus.REJECTED,result.status());
  assertEquals(ApplicationStage.REJECTED,app.getStage());
 }
 @Test void expiredIssuedOfferIsMarkedExpiredInsteadOfAccepted() {
  offer=new Offer("OFF-2026-ABC12345",app,new BigDecimal("900000"),"INR",LocalDate.now().minusDays(1));
  offer.submitForApproval(); offer.approve("approver",OffsetDateTime.now().minusDays(3));
  offer.issue(OffsetDateTime.now().minusDays(2));
  when(offers.findById(offer.getId())).thenReturn(Optional.of(offer));
  var result=service.recordResponse(offer.getId(),new OfferResponseRequest(OfferStatus.ACCEPTED,"candidate"));
  assertEquals(OfferStatus.EXPIRED,result.status());
  assertEquals(ApplicationStage.SELECTED,app.getStage());
 }
 @Test void unknownOfferReturnsNotFound() {
  when(offers.findById("missing")).thenReturn(Optional.empty());
  assertThrows(ResourceNotFoundException.class,()->service.getOffer("missing"));
 }
}
