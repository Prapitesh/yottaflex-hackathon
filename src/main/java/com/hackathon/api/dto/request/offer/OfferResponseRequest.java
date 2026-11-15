package com.hackathon.api.dto.request.offer;
import com.hackathon.api.enums.OfferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record OfferResponseRequest(
 @NotNull OfferStatus response,
 @NotBlank @Size(max=120) String actor) { }
