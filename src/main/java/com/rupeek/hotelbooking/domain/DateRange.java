package com.rupeek.hotelbooking.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }

    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }
}