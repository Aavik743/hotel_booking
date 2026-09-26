package com.rupeek.hotelbooking.api.dto;

import com.rupeek.hotelbooking.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to pay for an existing PENDING_PAYMENT booking. Sent together with the Idempotency-Key header.")
public record PaymentRequest(
        @Schema(description = "Payment method to charge", example = "CARD")
        @NotNull PaymentMethod method) {
}