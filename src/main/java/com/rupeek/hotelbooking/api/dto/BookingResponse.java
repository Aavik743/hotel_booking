package com.rupeek.hotelbooking.api.dto;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Current state of a booking, returned by the create/get/pay/cancel booking endpoints.")
public record BookingResponse(
        @Schema(description = "Booking id, use this for GET/payment/cancel calls", example = "5b1f7a2e-4e9d-4a2a-9f1b-1a2b3c4d5e6f") UUID id,
        @Schema(description = "Id of the booked property") UUID propertyId,
        @Schema(description = "Id of the booked room type") UUID roomTypeId,
        @Schema(description = "Booked date range") DateRange stay,
        @Schema(description = "Number of guests", example = "2") int guests,
        @Schema(description = "Total price for the stay") Money total,
        @Schema(description = "Current lifecycle status of the booking", example = "PENDING_PAYMENT") BookingStatus status,
        @Schema(description = "Timestamp the booking was created") LocalDateTime createdAt) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.id(), booking.propertyId(), booking.roomTypeId(), booking.stay(),
                booking.guests(), booking.total(), booking.status(), booking.createdAt());
    }
}