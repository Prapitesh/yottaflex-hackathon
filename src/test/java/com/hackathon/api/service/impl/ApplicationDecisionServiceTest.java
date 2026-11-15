package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.application.ApplicationDecisionRequest;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationDecisionServiceTest {
 @Mock private ApplicationRepository applications;
 @Mock private JobRequisitionRepository requisitions;
 @Mock private CandidateRepository candidates;
 @Mock private ResumeArtifactRepository resumes;
 @Mock private ApplicationStageHistoryRepository history;
 @Mock private AuditEventRepository audit;
 @Mock private DataIssueRepository issues;
 @Mock private OfferRepository offers;
 private ApplicationServiceImpl service;
 private Application application;

 @BeforeEach void setUp() {
  MockitoAnnotations.openMocks(this);
  service=new ApplicationServiceImpl(applications,requisitions,candidates,resumes,history,audit,issues,offers);
  Department department=new Department("DEP-1","Engineering","ENG");
  JobRequisition req=new JobRequisition("REQ-1",department,"Java Engineer","Build",
   EmploymentType.FULL_TIME,"Hyderabad",1,new BigDecimal("600000"),
   new BigDecimal("1000000"),"INR","requester");
  Candidate candidate=new Candidate("CAND-1","Asha Rao","asha@example.test",null,"Hyderabad",24);
  application=new Application("APP-REQ-1-CAND-1",req,candidate,null);
  application.changeStage(ApplicationStage.SCREENING,OffsetDateTime.now());
  when(applications.findById(application.getId())).thenReturn(Optional.of(application));
  when(applications.save(any(Application.class))).thenAnswer(i->i.getArgument(0));
 }
 @Test void recruiterCanShortlistAfterScreening() {
  var result=service.decideApplication(application.getId(),
   new ApplicationDecisionRequest(ApplicationStage.SHORTLISTED,"recruiter@example.test","Screening passed"));
  assertEquals(ApplicationStage.SHORTLISTED,result.stage());
  verify(history).save(any(ApplicationStageHistory.class));
  verify(audit).save(any(AuditEvent.class));
 }
 @Test void recruiterCanRejectAfterScreening() {
  var result=service.decideApplication(application.getId(),
   new ApplicationDecisionRequest(ApplicationStage.REJECTED,"recruiter@example.test","Does not meet role requirements"));
  assertEquals(ApplicationStage.REJECTED,result.stage());
 }
 @Test void cannotShortlistBeforeScreening() {
  application.changeStage(ApplicationStage.APPLIED,OffsetDateTime.now());
  assertThrows(IllegalStateException.class,()->service.decideApplication(application.getId(),
   new ApplicationDecisionRequest(ApplicationStage.SHORTLISTED,"recruiter@example.test","Pass")));
  verify(applications,never()).save(any(Application.class));
 }
 @Test void recruiterCannotSetServerControlledStage() {
  assertThrows(IllegalArgumentException.class,()->service.decideApplication(application.getId(),
   new ApplicationDecisionRequest(ApplicationStage.OFFERED,"recruiter@example.test","Skip process")));
 }
 @Test void unknownApplicationReturnsNotFound() {
  when(applications.findById("missing")).thenReturn(Optional.empty());
  assertThrows(ResourceNotFoundException.class,()->service.decideApplication("missing",
   new ApplicationDecisionRequest(ApplicationStage.REJECTED,"recruiter@example.test","No")));
 }
 @Test void withdrawingOfferedApplicationAlsoWithdrawsItsActiveOffer() {
  application.changeStage(ApplicationStage.OFFERED,OffsetDateTime.now());
  Offer activeOffer=new Offer("OFF-2026-TEST0001",application,new BigDecimal("850000"),
   "INR",LocalDate.now().plusDays(5));
  activeOffer.submitForApproval();
  activeOffer.approve("approver@example.test",OffsetDateTime.now().minusMinutes(2));
  activeOffer.issue(OffsetDateTime.now().minusMinutes(1));
  when(offers.findByApplication_IdOrderByCreatedAtDesc(application.getId()))
   .thenReturn(List.of(activeOffer));
  service.withdrawApplication(application.getId(),"candidate@example.test","Candidate withdrew");
  assertEquals(OfferStatus.WITHDRAWN,activeOffer.getStatus());
  assertEquals(ApplicationStage.WITHDRAWN,application.getStage());
  verify(offers).save(activeOffer);
 }
}
