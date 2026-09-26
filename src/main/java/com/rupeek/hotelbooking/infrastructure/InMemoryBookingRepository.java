package com.rupeek.hotelbooking.infrastructure;

import com.rupeek.hotelbooking.application.port.BookingRepository;
import com.rupeek.hotelbooking.domain.Booking;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentHashMap<UUID, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public Booking save(Booking booking) {
        bookings.put(booking.id(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return List.copyOf(bookings.values());
    }
}