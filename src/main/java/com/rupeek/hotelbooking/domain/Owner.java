package com.rupeek.hotelbooking.domain;

import java.util.List;
import java.util.UUID;

public record Owner(UUID id, String name, List<Property> properties) {

    public Owner {
        if (id == null || name == null || name.isBlank() || properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Owner must have a name and at least one property");
        }
        properties = List.copyOf(properties);
    }
}