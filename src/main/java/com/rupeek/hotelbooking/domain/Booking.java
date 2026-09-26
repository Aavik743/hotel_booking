package com.rupeek.hotelbooking.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public final class Booking {
    private final UUID id;
    private final UUID propertyId;
    private final UUID roomTypeId;
    private final DateRange stay;
    private final int guests;
    private final Money total;
    private final LocalDateTime createdAt;
    private BookingStatus status;

    public Booking(UUID id, UUID propertyId, UUID roomTypeId, DateRange stay, int guests,
                   Money total, LocalDateTime createdAt) {
        if (id == null || propertyId == null || roomTypeId == null || stay == null || guests < 1
                || total == null || createdAt == null) {
            throw new IllegalArgumentException("Booking details are invalid");
        }
        this.id = id;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.stay = stay;
        this.guests = guests;
        this.total = total;
        this.createdAt = createdAt;
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    public void confirmPayment() {
        requireStatus(BookingStatus.PENDING_PAYMENT);
        status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != BookingStatus.PENDING_PAYMENT && status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only active bookings can be cancelled");
        }
        status = BookingStatus.CANCELLED;
    }

    private void requireStatus(BookingStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Booking must be " + expected);
        }
    }

    public UUID id() { return id; }
    public UUID propertyId() { return propertyId; }
    public UUID roomTypeId() { return roomTypeId; }
    public DateRange stay() { return stay; }
    public int guests() { return guests; }
    public Money total() { return total; }
    public LocalDateTime createdAt() { return createdAt; }
    public BookingStatus status() { return status; }
}