package com.accesosport.payment.domain.model;

import com.accesosport.registration.domain.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentTest {

    @Test
    void createUsesTheProvidedPaymentId() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.create(
                paymentId,
                UUID.randomUUID(),
                "cs_test_123",
                new BigDecimal("300.00"),
                new BigDecimal("24.00")
        );

        assertEquals(paymentId, payment.getId());
        assertEquals(new BigDecimal("324.00"), payment.getAmountTotal());
    }

    @Test
    void failedCardRefundCanBeRetried() {
        Payment payment = Payment.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "cs_test_123",
                new BigDecimal("300.00"),
                new BigDecimal("24.00")
        );
        payment.confirm("pi_test_123", PaymentMethod.CARD);
        payment.initiateRefund();
        payment.failRefund("temporary failure");

        payment.initiateRefund();

        assertEquals(PaymentStatus.REFUND_PENDING, payment.getStatus());
        assertNull(payment.getRefundError());
        assertEquals(1, payment.getRefundAttempts());
    }
}
