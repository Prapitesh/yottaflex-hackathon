package com.hackathon.api.dto.request.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Required explanation for withdrawing an application. */
public record WithdrawApplicationRequest(
        @NotBlank(message = "Actor is required") @Size(max = 120)
        String actor,
        @NotBlank(message = "Withdrawal reason is required") @Size(max = 1000)
        String reason
) {
}
