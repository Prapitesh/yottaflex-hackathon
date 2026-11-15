package com.hackathon.api.dto.response.offer;
import com.hackathon.api.enums.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
public record OfferResponse(String id, String applicationId, BigDecimal salary,
 String currency, OfferStatus status, String approvedBy, OffsetDateTime approvedAt,
 OffsetDateTime issuedAt, OffsetDateTime respondedAt, LocalDate validUntil,
 OffsetDateTime createdAt) { }
