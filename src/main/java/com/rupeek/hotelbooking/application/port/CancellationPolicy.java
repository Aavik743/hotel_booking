package com.rupeek.hotelbooking.application.port;

import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.Money;

public interface CancellationPolicy {
    Money refundFor(Booking booking);
}