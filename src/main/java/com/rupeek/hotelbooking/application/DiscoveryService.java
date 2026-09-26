package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.application.port.BookingRepository;
import com.rupeek.hotelbooking.application.port.OwnerRepository;
import com.rupeek.hotelbooking.application.port.PricingStrategy;
import com.rupeek.hotelbooking.api.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.BookingStatus;
import com.rupeek.hotelbooking.domain.Property;
import com.rupeek.hotelbooking.domain.RoomType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DiscoveryService {
    private final OwnerRepository ownerRepository;
    private final BookingRepository bookingRepository;
    private final PricingStrategy pricingStrategy;

    public DiscoveryService(OwnerRepository ownerRepository, BookingRepository bookingRepository,
                            PricingStrategy pricingStrategy) {
        this.ownerRepository = ownerRepository;
        this.bookingRepository = bookingRepository;
        this.pricingStrategy = pricingStrategy;
    }

    public List<Property> search(SearchCriteria criteria) {
        return ownerRepository.findAll().stream()
                .flatMap(owner -> owner.properties().stream())
                .filter(property -> matches(property, criteria))
                .toList();
    }

    public Property getProperty(java.util.UUID propertyId) {
        return ownerRepository.findAll().stream()
                .flatMap(owner -> owner.properties().stream())
                .filter(property -> property.id().equals(propertyId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
    }

    private boolean matches(Property property, SearchCriteria criteria) {
        if (criteria.city() != null && !property.location().city().equalsIgnoreCase(criteria.city())) {
            return false;
        }
        if (criteria.locality() != null && !property.location().locality().equalsIgnoreCase(criteria.locality())) {
            return false;
        }
        if (criteria.minStarRating() != null && property.starRating() < criteria.minStarRating()) {
            return false;
        }
        if (!property.amenities().containsAll(criteria.amenities())) {
            return false;
        }
        return property.roomTypes().stream().anyMatch(roomType -> matches(roomType, property, criteria));
    }

    private boolean matches(RoomType roomType, Property property, SearchCriteria criteria) {
        if (criteria.guests() != null && roomType.maxGuests() < criteria.guests()) {
            return false;
        }
        BigDecimal price = pricingStrategy.pricePerNight(roomType, criteria.stay()).amount();
        if (criteria.minPrice() != null && price.compareTo(criteria.minPrice()) < 0) {
            return false;
        }
        if (criteria.maxPrice() != null && price.compareTo(criteria.maxPrice()) > 0) {
            return false;
        }
        return criteria.stay() == null || available(roomType, criteria);
    }

    private boolean available(RoomType roomType, SearchCriteria criteria) {
        long usedInventory = bookingRepository.findAll().stream()
                .filter(booking -> booking.roomTypeId().equals(roomType.id()))
                .filter(booking -> booking.status() != BookingStatus.CANCELLED)
                .filter(booking -> booking.stay().overlaps(criteria.stay()))
                .count();
        return usedInventory < roomType.inventory();
    }
}