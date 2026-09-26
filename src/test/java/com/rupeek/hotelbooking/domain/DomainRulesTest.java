package com.rupeek.hotelbooking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainRulesTest {
    @Test
    void adjacentDateRangesDoNotOverlap() {
        DateRange first = new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
        DateRange second = new DateRange(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 5));

        assertFalse(first.overlaps(second));
        assertEquals(2, first.nights());
    }

    @Test
    void invalidDateRangeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 3)));
    }

    @Test
    void bookingOnlyConfirmsFromPendingPayment() {
        Booking booking = booking();

        booking.confirmPayment();
        assertEquals(BookingStatus.CONFIRMED, booking.status());
        booking.cancel();
        assertEquals(BookingStatus.CANCELLED, booking.status());
        assertThrows(IllegalStateException.class, booking::cancel);
    }

    private Booking booking() {
        return new Booking(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2)), 1,
                new Money(new BigDecimal("100"), "INR"), LocalDateTime.now());
    }
}