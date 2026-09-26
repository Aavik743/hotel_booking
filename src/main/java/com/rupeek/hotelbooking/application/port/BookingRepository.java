package com.rupeek.hotelbooking.application.port;

import com.rupeek.hotelbooking.domain.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
    List<Booking> findAll();
}