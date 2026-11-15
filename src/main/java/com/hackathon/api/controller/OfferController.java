package com.hackathon.api.controller;
import com.hackathon.api.dto.request.offer.*;
import com.hackathon.api.dto.response.offer.OfferResponse;
import com.hackathon.api.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1")
public class OfferController {
 private final OfferService offerService;
 public OfferController(OfferService offerService) { this.offerService=offerService; }
 @PostMapping("/offers")
 public ResponseEntity<OfferResponse> create(@Valid @RequestBody CreateOfferRequest request) {
  return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(request));
 }
 @PostMapping("/offers/{offerId}/submit-for-approval")
 public ResponseEntity<OfferResponse> submit(@PathVariable String offerId,@Valid @RequestBody OfferActorRequest request) {
  return ResponseEntity.ok(offerService.submitForApproval(offerId,request));
 }
 @PostMapping("/offers/{offerId}/approve")
 public ResponseEntity<OfferResponse> approve(@PathVariable String offerId,@Valid @RequestBody OfferActorRequest request) {
  return ResponseEntity.ok(offerService.approveOffer(offerId,request));
 }
 @PostMapping("/offers/{offerId}/issue")
 public ResponseEntity<OfferResponse> issue(@PathVariable String offerId,@Valid @RequestBody OfferActorRequest request) {
  return ResponseEntity.ok(offerService.issueOffer(offerId,request));
 }
 @PostMapping("/offers/{offerId}/response")
 public ResponseEntity<OfferResponse> respond(@PathVariable String offerId,@Valid @RequestBody OfferResponseRequest request) {
  return ResponseEntity.ok(offerService.recordResponse(offerId,request));
 }
 @GetMapping("/offers")
 public ResponseEntity<List<OfferResponse>> getAll() { return ResponseEntity.ok(offerService.getAllOffers()); }
 @GetMapping("/offers/{offerId}")
 public ResponseEntity<OfferResponse> get(@PathVariable String offerId) {
  return ResponseEntity.ok(offerService.getOffer(offerId));
 }
 @GetMapping("/applications/{applicationId}/offers")
 public ResponseEntity<List<OfferResponse>> getForApplication(@PathVariable String applicationId) {
  return ResponseEntity.ok(offerService.getOffersForApplication(applicationId));
 }
}
