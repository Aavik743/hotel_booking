package com.rupeek.hotelbooking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request to create a booking. propertyId/roomTypeId must come from POST /api/v1/owners or GET /api/v1/properties/search.")
public record CreateBookingRequest(
        @Schema(description = "Id of an existing property, obtained from the owners or properties API", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull UUID propertyId,
        @Schema(description = "Id of a room type belonging to the property, obtained from the owners or properties API", example = "9c858901-8a57-4791-81fe-4c455b099bc9")
        @NotNull UUID roomTypeId,
        @Schema(description = "Stay start date (inclusive), must not be in the past", example = "2026-10-01")
        @NotNull @FutureOrPresent LocalDate checkIn,
        @Schema(description = "Stay end date (exclusive), must not be in the past", example = "2026-10-05")
        @NotNull @FutureOrPresent LocalDate checkOut,
        @Schema(description = "Number of guests staying, must not exceed the room type's maxGuests", example = "2")
        @Min(1) int guests) {
}