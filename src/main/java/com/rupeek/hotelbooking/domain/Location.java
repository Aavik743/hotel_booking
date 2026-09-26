package com.rupeek.hotelbooking.domain;

public record Location(String city, String locality) {

    public Location {
        if (city == null || city.isBlank() || locality == null || locality.isBlank()) {
            throw new IllegalArgumentException("city and locality are required");
        }
    }
}