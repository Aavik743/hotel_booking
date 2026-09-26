package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.application.port.BookingRepository;
import com.rupeek.hotelbooking.application.port.CancellationPolicy;
import com.rupeek.hotelbooking.application.port.OwnerRepository;
import com.rupeek.hotelbooking.application.port.PaymentGateway;
import com.rupeek.hotelbooking.application.port.PaymentResult;
import com.rupeek.hotelbooking.application.port.PricingStrategy;
import com.rupeek.hotelbooking.api.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BookingService {
    private final OwnerRepository ownerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final CancellationPolicy cancellationPolicy;
    private final PricingStrategy pricingStrategy;
    private final Clock clock;
    private final ReentrantLock bookingLock = new ReentrantLock();
    private final Map<String, PaymentRecord> paymentsByKey = new HashMap<>();

    public BookingService(OwnerRepository ownerRepository, BookingRepository bookingRepository,
                          PaymentGateway paymentGateway, CancellationPolicy cancellationPolicy,
                          PricingStrategy pricingStrategy) {
        this.ownerRepository = ownerRepository;
        this.bookingRepository = bookingRepository;
        this.paymentGateway = paymentGateway;
        this.cancellationPolicy = cancellationPolicy;
        this.pricingStrategy = pricingStrategy;
        this.clock = Clock.systemUTC();
    }

    public Booking create(UUID propertyId, UUID roomTypeId, com.rupeek.hotelbooking.domain.DateRange stay, int guests) {
        bookingLock.lock();
        try {
            RoomType roomType = findRoomType(propertyId, roomTypeId);
            if (guests < 1 || guests > roomType.maxGuests()) {
                throw new IllegalArgumentException("Guest count exceeds room capacity");
            }
            long usedInventory = bookingRepository.findAll().stream()
                    .filter(booking -> booking.roomTypeId().equals(roomTypeId))
                    .filter(booking -> booking.status() != BookingStatus.CANCELLED)
                    .filter(booking -> booking.stay().overlaps(stay))
                    .count();
            if (usedInventory >= roomType.inventory()) {
                throw new IllegalStateException("No inventory available for requested dates");
            }
            Money total = pricingStrategy.pricePerNight(roomType, stay).multiply(stay.nights());
            Booking booking = new Booking(UUID.randomUUID(), propertyId, roomTypeId, stay, guests,
                    total, LocalDateTime.now(clock));
            return bookingRepository.save(booking);
        } finally {
            bookingLock.unlock();
        }
    }

    public Booking pay(UUID bookingId, PaymentMethod method, String idempotencyKey) {
        bookingLock.lock();
        try {
            PaymentRecord previous = paymentsByKey.get(idempotencyKey);
            if (previous != null) {
                if (!previous.bookingId().equals(bookingId) || previous.method() != method) {
                    throw new IllegalStateException("Idempotency key was already used for another payment");
                }
                return previous.booking();
            }

            Booking booking = get(bookingId);
            PaymentResult result = paymentGateway.charge(booking.id(), booking.total(), method);
            if (result.successful()) {
                booking.confirmPayment();
            } else {
                booking.cancel();
            }
            bookingRepository.save(booking);
            paymentsByKey.put(idempotencyKey, new PaymentRecord(bookingId, method, booking));
            return booking;
        } finally {
            bookingLock.unlock();
        }
    }

    public Booking cancel(UUID bookingId) {
        Booking booking = get(bookingId);
        if (!booking.stay().checkIn().isAfter(java.time.LocalDate.now(clock))) {
            throw new IllegalStateException("Booking can only be cancelled before check-in");
        }
        if (booking.status() == BookingStatus.CONFIRMED) {
            Money refund = cancellationPolicy.refundFor(booking);
            PaymentResult result = paymentGateway.refund(booking.id(), refund);
            if (!result.successful()) {
                throw new IllegalStateException("Refund failed: " + result.message());
            }
        }
        booking.cancel();
        return bookingRepository.save(booking);
    }

    public Booking get(UUID bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
    }

    private RoomType findRoomType(UUID propertyId, UUID roomTypeId) {
        Property property = ownerRepository.findAll().stream()
                .flatMap(owner -> owner.properties().stream())
                .filter(candidate -> candidate.id().equals(propertyId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        return property.roomTypes().stream()
                .filter(roomType -> roomType.id().equals(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + roomTypeId));
    }

    private record PaymentRecord(UUID bookingId, PaymentMethod method, Booking booking) {
    }
}