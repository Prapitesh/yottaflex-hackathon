package com.hackathon.api.dto.request.offer;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record CreateOfferRequest(
 @NotBlank @Size(max=100) String applicationId,
 @NotNull @DecimalMin("0.01") BigDecimal salary,
 @NotBlank @Pattern(regexp="^[A-Za-z]{3}$") String currency,
 @NotNull @FutureOrPresent LocalDate validUntil,
 @NotBlank @Size(max=120) String actor) { }
