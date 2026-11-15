package com.hackathon.api.service;
import com.hackathon.api.dto.request.offer.*;
import com.hackathon.api.dto.response.offer.OfferResponse;
import java.util.List;
public interface OfferService {
 OfferResponse createOffer(CreateOfferRequest request);
 OfferResponse submitForApproval(String offerId, OfferActorRequest request);
 OfferResponse approveOffer(String offerId, OfferActorRequest request);
 OfferResponse issueOffer(String offerId, OfferActorRequest request);
 OfferResponse recordResponse(String offerId, OfferResponseRequest request);
 OfferResponse getOffer(String offerId);
 List<OfferResponse> getOffersForApplication(String applicationId);
 List<OfferResponse> getAllOffers();
}
