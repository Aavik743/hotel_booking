package com.rupeek.hotelbooking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "A room type offered by a property, with its price and inventory. Stored in INR.")
public record RoomTypeRequest(
        @Schema(description = "Room type display name", example = "Deluxe Room")
        @NotBlank String name,
        @Schema(description = "Price per night in INR", example = "3500.00")
        @NotNull @DecimalMin("0.01") BigDecimal pricePerNight,
        @Schema(description = "Maximum guests this room type can accommodate; bookings with more guests are rejected", example = "3")
        @Min(1) int maxGuests,
        @Schema(description = "Total number of rooms of this type available to book", example = "5")
        @Min(1) int inventory) {
}