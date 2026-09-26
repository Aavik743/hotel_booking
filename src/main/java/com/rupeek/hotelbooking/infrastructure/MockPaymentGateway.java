package com.rupeek.hotelbooking.infrastructure;

import com.rupeek.hotelbooking.application.port.PaymentGateway;
import com.rupeek.hotelbooking.application.port.PaymentResult;
import com.rupeek.hotelbooking.domain.Money;
import com.rupeek.hotelbooking.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(UUID bookingId, Money amount, PaymentMethod method) {
        return new PaymentResult(true, "Mock payment accepted");
    }

    @Override
    public PaymentResult refund(UUID bookingId, Money amount) {
        return new PaymentResult(true, "Mock refund accepted");
    }
}