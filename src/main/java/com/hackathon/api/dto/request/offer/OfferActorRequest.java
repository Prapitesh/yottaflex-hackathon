package com.hackathon.api.dto.request.offer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record OfferActorRequest(@NotBlank @Size(max=120) String actor) { }
