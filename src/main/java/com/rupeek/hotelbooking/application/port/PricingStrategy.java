package com.rupeek.hotelbooking.application.port;

import com.rupeek.hotelbooking.domain.DateRange;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.RoomType;

@FunctionalInterface
public interface PricingStrategy {
    Money pricePerNight(RoomType roomType, DateRange stay);
}