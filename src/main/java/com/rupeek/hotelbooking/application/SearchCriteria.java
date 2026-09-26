package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.DateRange;

import java.math.BigDecimal;
import java.util.Set;

public record SearchCriteria(String city, String locality, DateRange stay, Integer guests,
                             BigDecimal minPrice, BigDecimal maxPrice, Set<String> amenities,
                             Integer minStarRating) {

    public SearchCriteria {
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }
}