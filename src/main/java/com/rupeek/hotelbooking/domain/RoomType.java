package com.rupeek.hotelbooking.domain;

import java.util.UUID;

public record RoomType(UUID id, String name, Money pricePerNight, int maxGuests, int inventory) {

    public RoomType {
        if (id == null || name == null || name.isBlank() || pricePerNight == null
                || maxGuests < 1 || inventory < 1) {
            throw new IllegalArgumentException("Room type details are invalid");
        }
    }
}