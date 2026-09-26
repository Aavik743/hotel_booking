package com.rupeek.hotelbooking.infrastructure;

import com.rupeek.hotelbooking.application.port.PricingStrategy;
import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;
import org.springframework.stereotype.Component;

@Component
public class FixedPricingStrategy implements PricingStrategy {
    @Override
    public Money pricePerNight(RoomType roomType, DateRange stay) {
        return roomType.pricePerNight();
    }
}