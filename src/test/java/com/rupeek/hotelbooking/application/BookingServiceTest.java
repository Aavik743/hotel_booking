package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.application.port.CancellationPolicy;
import com.rupeek.hotelbooking.application.port.PaymentGateway;
import com.rupeek.hotelbooking.application.port.PaymentResult;
import com.rupeek.hotelbooking.application.port.PricingStrategy;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Location;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.Owner;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import com.rupeek.hotelbooking.infrastructure.InMemoryBookingRepository;
import com.rupeek.hotelbooking.infrastructure.InMemoryOwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingServiceTest {
    private final InMemoryOwnerRepository owners = new InMemoryOwnerRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private BookingService service;
    private UUID propertyId;
    private UUID roomTypeId;

    @BeforeEach
    void setUp() {
        UUID ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        roomTypeId = UUID.randomUUID();
        RoomType roomType = new RoomType(roomTypeId, "Deluxe", Money.of("100", "INR"), 2, 1);
        Property property = new Property(propertyId, "Test Hotel", new Location("Pune", "Baner"),
                4, List.of("WIFI"), List.of(roomType));
        owners.save(new Owner(ownerId, "Owner", List.of(property)));
        PaymentGateway payments = new PaymentGateway() {
            public PaymentResult charge(UUID id, Money amount, PaymentMethod method) {
                return new PaymentResult(true, "ok");
            }

            public PaymentResult refund(UUID id, Money amount) {
                return new PaymentResult(true, "ok");
            }
        };
        CancellationPolicy policy = Booking::total;
        PricingStrategy pricing = (room, stay) -> room.pricePerNight();
        service = new BookingService(owners, bookings, payments, policy, pricing);
    }

    @Test
    void inventoryIsReservedAndReleasedOnCancellation() {
        DateRange stay = stay();
        Booking booking = service.create(propertyId, roomTypeId, stay, 2);

        assertThrows(IllegalStateException.class, () -> service.create(propertyId, roomTypeId, stay, 1));
        service.cancel(booking.id());
        Booking replacement = service.create(propertyId, roomTypeId, stay, 1);

        assertEquals(BookingStatus.PENDING_PAYMENT, replacement.status());
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        Booking booking = service.create(propertyId, roomTypeId, stay(), 1);

        Booking paid = service.pay(booking.id(), PaymentMethod.UPI, "payment-1");

        assertEquals(BookingStatus.CONFIRMED, paid.status());
    }

    @Test
    void simultaneousRequestsForFinalRoomAllowOnlyOneBooking() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier start = new CyclicBarrier(3);
        Callable<Boolean> attempt = () -> {
            start.await();
            try {
                service.create(propertyId, roomTypeId, stay(), 1);
                return true;
            } catch (IllegalStateException exception) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(attempt);
            Future<Boolean> second = executor.submit(attempt);
            start.await();

            assertEquals(1, Stream.of(first.get(), second.get()).filter(Boolean::booleanValue).count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void repeatedPaymentKeyChargesOnlyOnce() {
        AtomicInteger charges = new AtomicInteger();
        PaymentGateway payments = new PaymentGateway() {
            public PaymentResult charge(UUID id, Money amount, PaymentMethod method) {
                charges.incrementAndGet();
                return new PaymentResult(true, "ok");
            }

            public PaymentResult refund(UUID id, Money amount) {
                return new PaymentResult(true, "ok");
            }
        };
        service = new BookingService(owners, bookings, payments, Booking::total,
                (room, stay) -> room.pricePerNight());
        Booking booking = service.create(propertyId, roomTypeId, stay(), 1);

        service.pay(booking.id(), PaymentMethod.UPI, "payment-1");
        service.pay(booking.id(), PaymentMethod.UPI, "payment-1");

        assertEquals(1, charges.get());
    }

    @Test
    void cancellationIsRejectedOnOrAfterCheckIn() {
        DateRange pastStay = new DateRange(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1));
        Booking booking = new Booking(UUID.randomUUID(), propertyId, roomTypeId, pastStay, 1,
                Money.of("100", "INR"), LocalDateTime.now());
        bookings.save(booking);

        assertThrows(IllegalStateException.class, () -> service.cancel(booking.id()));
    }

    @Test
    void pricingStrategySetsBookingTotal() {
        PaymentGateway payments = new PaymentGateway() {
            public PaymentResult charge(UUID id, Money amount, PaymentMethod method) {
                return new PaymentResult(true, "ok");
            }

            public PaymentResult refund(UUID id, Money amount) {
                return new PaymentResult(true, "ok");
            }
        };
        service = new BookingService(owners, bookings, payments,
                Booking::total, (room, stay) -> Money.of("125", "INR"));

        Booking booking = service.create(propertyId, roomTypeId, stay(), 1);

        assertEquals(Money.of("250", "INR"), booking.total());
    }

    private DateRange stay() {
        return new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
    }
}