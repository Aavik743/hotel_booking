package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.infrastructure.InMemoryBookingRepository;
import com.rupeek.hotelbooking.infrastructure.InMemoryOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscoveryServiceTest {
    private final InMemoryOwnerRepository owners = new InMemoryOwnerRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private DiscoveryService service;
    private Property availableProperty;
    private Property unavailableProperty;

    @BeforeEach
    void setUp() {
        RoomType availableRoom = new RoomType(UUID.randomUUID(), "Deluxe", Money.of("100", "INR"), 2, 1);
        availableProperty = new Property(UUID.randomUUID(), "Pune Suites", new Location("Pune", "Baner"),
                5, List.of("WIFI", "POOL"), List.of(availableRoom));

        RoomType unavailableRoom = new RoomType(UUID.randomUUID(), "Standard", Money.of("80", "INR"), 2, 1);
        unavailableProperty = new Property(UUID.randomUUID(), "Pune Central", new Location("Pune", "Kothrud"),
                3, List.of("WIFI"), List.of(unavailableRoom));

        owners.save(new Owner(UUID.randomUUID(), "Owner", List.of(availableProperty, unavailableProperty)));
        bookings.save(new Booking(UUID.randomUUID(), unavailableProperty.id(), unavailableRoom.id(),
                stay(), 1, unavailableRoom.pricePerNight().multiply(stay().nights()), LocalDateTime.now()));
        service = new DiscoveryService(owners, bookings, (room, stay) -> room.pricePerNight());
    }

    @Test
    void searchAppliesLocationPriceAmenitiesRatingAndGuestFilters() {
        SearchCriteria criteria = new SearchCriteria("pune", "BANER", stay(), 2,
                new BigDecimal("90"), new BigDecimal("110"), Set.of("WIFI", "POOL"), 4);

        List<Property> results = service.search(criteria);

        assertEquals(List.of(availableProperty), results);
    }

    @Test
    void searchExcludesUnavailablePropertyButAllowsAdjacentStay() {
        SearchCriteria occupiedDates = new SearchCriteria("Pune", null, stay(), 1,
                null, null, Set.of(), null);
        SearchCriteria adjacentDates = new SearchCriteria("Pune", null,
                new DateRange(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 5)), 1,
                null, null, Set.of(), null);

        assertEquals(List.of(availableProperty), service.search(occupiedDates));
        assertEquals(List.of(availableProperty, unavailableProperty), service.search(adjacentDates));
    }

    private DateRange stay() {
        return new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
    }
}