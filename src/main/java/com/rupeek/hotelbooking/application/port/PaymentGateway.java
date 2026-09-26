package com.rupeek.hotelbooking.application.port;

import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;

import java.util.UUID;

public interface PaymentGateway {
    PaymentResult charge(UUID bookingId, Money amount, PaymentMethod method);
    PaymentResult refund(UUID bookingId, Money amount);
}