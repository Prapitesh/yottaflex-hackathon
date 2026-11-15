package com.hackathon.api.service.impl;
import com.hackathon.api.dto.request.offer.*;
import com.hackathon.api.dto.response.offer.OfferResponse;
import com.hackathon.api.entity.*;
import com.hackathon.api.enums.*;
import com.hackathon.api.exception.ResourceNotFoundException;
import com.hackathon.api.repository.*;
import com.hackathon.api.service.OfferService;
import com.hackathon.api.policy.RecruitmentPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OfferServiceImpl implements OfferService {
 private final OfferRepository offerRepository;
 private final ApplicationRepository applicationRepository;
 private final ApplicationStageHistoryRepository historyRepository;
 private final AuditEventRepository auditRepository;
 public OfferServiceImpl(OfferRepository offers,ApplicationRepository applications,
  ApplicationStageHistoryRepository history,AuditEventRepository audit) {
  this.offerRepository=offers; this.applicationRepository=applications;
  this.historyRepository=history; this.auditRepository=audit;
 }
 @Override
 public OfferResponse createOffer(CreateOfferRequest request) {
  Application app=findApplication(request.applicationId());
  if(app.getStage()!=ApplicationStage.SELECTED)
   throw new IllegalStateException("An offer can only be created for a SELECTED application");
  var req=app.getRequisition();
  String currency=request.currency().trim().toUpperCase();
  if(!RecruitmentPolicy.isSalaryWithinBand(request.salary(),req.getSalaryMin(),req.getSalaryMax()))
   throw new IllegalArgumentException("Offer salary must be within the approved requisition salary band");
  if(!currency.equalsIgnoreCase(req.getCurrency()))
   throw new IllegalArgumentException("Offer currency must match the requisition currency");
  boolean active=offerRepository.findByApplication_IdOrderByCreatedAtDesc(app.getId()).stream()
   .anyMatch(o->List.of(OfferStatus.DRAFT,OfferStatus.PENDING_APPROVAL,OfferStatus.APPROVED,OfferStatus.ISSUED).contains(o.getStatus()));
  if(active) throw new IllegalStateException("This application already has an active offer");
  Offer saved=offerRepository.save(new Offer(EntityIdGenerator.offerId(),app,request.salary(),currency,request.validUntil()));
  audit(saved,"OFFER_CREATED",request.actor(),Map.of("applicationId",app.getId(),"salary",request.salary(),
   "currency",currency,"validUntil",request.validUntil().toString()));
  return toResponse(saved);
 }
 @Override
 public OfferResponse submitForApproval(String id,OfferActorRequest request) {
  Offer offer=findOffer(id); requireStatus(offer,OfferStatus.DRAFT);
  offer.submitForApproval(); Offer saved=offerRepository.save(offer);
  audit(saved,"OFFER_SUBMITTED_FOR_APPROVAL",request.actor(),Map.of()); return toResponse(saved);
 }
 @Override
 public OfferResponse approveOffer(String id,OfferActorRequest request) {
  Offer offer=findOffer(id); requireStatus(offer,OfferStatus.PENDING_APPROVAL);
  if(offer.getValidUntil().isBefore(LocalDate.now())) throw new IllegalStateException("Expired offer cannot be approved");
  OffsetDateTime now=OffsetDateTime.now(); offer.approve(request.actor().trim(),now);
  Offer saved=offerRepository.save(offer);
  audit(saved,"OFFER_APPROVED",request.actor(),Map.of("approvedAt",now.toString())); return toResponse(saved);
 }
 @Override
 public OfferResponse issueOffer(String id,OfferActorRequest request) {
  Offer offer=findOffer(id); requireStatus(offer,OfferStatus.APPROVED);
  Application app=offer.getApplication();
  if(app.getStage()!=ApplicationStage.SELECTED)
   throw new IllegalStateException("Only a SELECTED application can receive an issued offer");
  if(offer.getValidUntil().isBefore(LocalDate.now())) throw new IllegalStateException("Offer validity date has passed");
  OffsetDateTime now=OffsetDateTime.now(); offer.issue(now);
  app.changeStage(ApplicationStage.OFFERED,now);
  offerRepository.save(offer); applicationRepository.save(app);
  historyRepository.save(new ApplicationStageHistory(app,ApplicationStage.SELECTED,ApplicationStage.OFFERED,
   request.actor().trim(),"Approved employment offer issued"));
  audit(offer,"OFFER_ISSUED",request.actor(),Map.of("applicationStage","OFFERED"));
  return toResponse(offer);
 }
 @Override
 public OfferResponse recordResponse(String id,OfferResponseRequest request) {
  Offer offer=findOffer(id);
  if(request.response()!=OfferStatus.ACCEPTED && request.response()!=OfferStatus.REJECTED)
   throw new IllegalArgumentException("Response must be ACCEPTED or REJECTED");
  if(offer.getStatus()==OfferStatus.ISSUED && offer.getValidUntil().isBefore(LocalDate.now())) {
   offer.expire(); offerRepository.save(offer);
   audit(offer,"OFFER_EXPIRED","SYSTEM",Map.of("validUntil",offer.getValidUntil().toString()));
   return toResponse(offer);
  }
  OffsetDateTime now=OffsetDateTime.now(); offer.recordResponse(request.response(),now);
  Application app=offer.getApplication();
  ApplicationStage target=request.response()==OfferStatus.ACCEPTED?ApplicationStage.HIRED:ApplicationStage.REJECTED;
  RecruitmentPolicy.requireValidTransition(app.getStage(),target);
  ApplicationStage previous=app.getStage(); app.changeStage(target,now);
  offerRepository.save(offer); applicationRepository.save(app);
  historyRepository.save(new ApplicationStageHistory(app,previous,target,request.actor().trim(),
   "Candidate "+request.response().name().toLowerCase()+" the offer"));
  audit(offer,request.response()==OfferStatus.ACCEPTED?"OFFER_ACCEPTED":"OFFER_REJECTED",
   request.actor(),Map.of("applicationStage",target.name()));
  return toResponse(offer);
 }
 @Override @Transactional(readOnly=true)
 public OfferResponse getOffer(String id) { return toResponse(findOffer(id)); }
 @Override @Transactional(readOnly=true)
 public List<OfferResponse> getOffersForApplication(String id) {
  if(!applicationRepository.existsById(id)) throw new ResourceNotFoundException("Application not found: "+id);
  return offerRepository.findByApplication_IdOrderByCreatedAtDesc(id).stream().map(this::toResponse).toList();
 }
 @Override @Transactional(readOnly=true)
 public List<OfferResponse> getAllOffers() { return offerRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList(); }
 private Application findApplication(String id) {
  return applicationRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Application not found: "+id));
 }
 private Offer findOffer(String id) {
  return offerRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Offer not found: "+id));
 }
 private void requireStatus(Offer o,OfferStatus expected) {
  if(o.getStatus()!=expected) throw new IllegalStateException("Offer must be "+expected+"; current status is "+o.getStatus());
 }
 private void audit(Offer o,String event,String actor,Map<String,Object> payload) {
  auditRepository.save(new AuditEvent("OFFER",o.getId(),event,actor.trim(),payload));
 }
 private OfferResponse toResponse(Offer o) {
  return new OfferResponse(o.getId(),o.getApplication().getId(),o.getSalary(),o.getCurrency(),o.getStatus(),
   o.getApprovedBy(),o.getApprovedAt(),o.getIssuedAt(),o.getRespondedAt(),o.getValidUntil(),o.getCreatedAt());
 }
}
