package com.rupeek.hotelbooking.api;

import com.rupeek.hotelbooking.api.dto.BookingResponse;
import com.rupeek.hotelbooking.api.dto.CreateBookingRequest;
import com.rupeek.hotelbooking.api.dto.PaymentRequest;
import com.rupeek.hotelbooking.application.BookingService;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.DateRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Create and manage bookings for a property's room type, and drive them through payment/cancellation.")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking",
            description = "Creates a new booking in PENDING_PAYMENT status for the given property and room type. "
                    + "`propertyId` and `roomTypeId` must come from a prior call to `GET /api/v1/properties/search` "
                    + "or `GET /api/v1/properties/{propertyId}` (owners create properties/room types via "
                    + "`POST /api/v1/owners`). Availability for the requested date range and inventory is validated "
                    + "server-side; no separate availability-check call is required beforehand.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created"),
            @ApiResponse(responseCode = "400", description = "Validation failure, e.g. checkOut before checkIn or guests exceeding room capacity"),
            @ApiResponse(responseCode = "404", description = "propertyId or roomTypeId does not exist"),
            @ApiResponse(responseCode = "409", description = "No inventory available for the requested date range")
    })
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.create(request.propertyId(), request.roomTypeId(),
                new DateRange(request.checkIn(), request.checkOut()), request.guests());
        return BookingResponse.from(booking);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get a booking by id",
            description = "Returns the current state of a booking. `bookingId` is the `id` returned by the create-booking call.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "No booking exists with the given id")
    })
    public BookingResponse get(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.get(bookingId));
    }

    @PostMapping("/{bookingId}/payment")
    @Operation(summary = "Pay for a booking",
            description = "Marks a PENDING_PAYMENT booking as CONFIRMED. `bookingId` must reference a booking previously "
                    + "returned by the create-booking call. The `Idempotency-Key` header is required and must be a "
                    + "client-generated unique value per payment attempt so that retries of the same request do not "
                    + "double-charge; replaying the same key for the same booking returns the original result instead "
                    + "of processing the payment again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment applied, booking is now CONFIRMED"),
            @ApiResponse(responseCode = "400", description = "Booking is not in a payable state (e.g. already paid or cancelled)"),
            @ApiResponse(responseCode = "404", description = "No booking exists with the given id")
    })
    public BookingResponse pay(@PathVariable UUID bookingId,
                               @Parameter(in = ParameterIn.HEADER, required = true,
                                       description = "Client-generated unique key used to safely retry this payment without double-charging",
                                       example = "8f14e45f-ceea-467e-9c99-example-key")
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody PaymentRequest request) {
        return BookingResponse.from(bookingService.pay(bookingId, request.method(), idempotencyKey));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking",
            description = "Cancels a booking and releases its inventory hold. `bookingId` must reference a booking "
                    + "previously returned by the create-booking call. Bookings that are already cancelled or completed "
                    + "cannot be cancelled again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled"),
            @ApiResponse(responseCode = "400", description = "Booking is not in a cancellable state"),
            @ApiResponse(responseCode = "404", description = "No booking exists with the given id")
    })
    public BookingResponse cancel(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.cancel(bookingId));
    }
}