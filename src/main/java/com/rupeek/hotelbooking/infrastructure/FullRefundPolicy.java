package com.rupeek.hotelbooking.infrastructure;

import com.rupeek.hotelbooking.application.port.CancellationPolicy;
import com.rupeek.hotelbooking.domain.Booking;
import com.rupeek.hotelbooking.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class FullRefundPolicy implements CancellationPolicy {
    @Override
    public Money refundFor(Booking booking) {
        return booking.total();
    }
}