package com.rupeek.hotelbooking.domain;

import java.util.List;
import java.util.UUID;

public record Property(UUID id, String name, Location location, int starRating,
                       List<String> amenities, List<RoomType> roomTypes) {

    public Property {
        if (id == null || name == null || name.isBlank() || location == null
                || starRating < 1 || starRating > 5 || roomTypes == null || roomTypes.isEmpty()) {
            throw new IllegalArgumentException("Property details are invalid");
        }
        amenities = amenities == null ? List.of() : List.copyOf(amenities);
        roomTypes = List.copyOf(roomTypes);
    }
}