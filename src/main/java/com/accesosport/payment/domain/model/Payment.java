package com.accesosport.payment.domain.model;

import com.accesosport.registration.domain.model.PaymentMethod;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Payment {

    private UUID id;
    private UUID registrationId;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private String stripeRefundId;
    private BigDecimal baseAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal amountTotal;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant confirmedAt;
    private Instant refundedAt;

    private Payment() {}

    public static Payment create(
            UUID registrationId,
            String stripeSessionId,
            BigDecimal baseAmount,
            BigDecimal serviceFeeAmount
    ) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.registrationId = registrationId;
        payment.stripeSessionId = stripeSessionId;
        payment.baseAmount = baseAmount;
        payment.serviceFeeAmount = serviceFeeAmount;
        payment.amountTotal = baseAmount.add(serviceFeeAmount);
        payment.currency = "mxn";
        payment.paymentMethod = null;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = Instant.now();
        payment.confirmedAt = null;
        payment.refundedAt = null;
        return payment;
    }

    public static Payment reconstitute(
            UUID id,
            UUID registrationId,
            String stripeSessionId,
            String stripePaymentIntentId,
            String stripeRefundId,
            BigDecimal baseAmount,
            BigDecimal serviceFeeAmount,
            BigDecimal amountTotal,
            String currency,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            Instant createdAt,
            Instant confirmedAt,
            Instant refundedAt
    ) {
        Payment payment = new Payment();
        payment.id = id;
        payment.registrationId = registrationId;
        payment.stripeSessionId = stripeSessionId;
        payment.stripePaymentIntentId = stripePaymentIntentId;
        payment.stripeRefundId = stripeRefundId;
        payment.baseAmount = baseAmount;
        payment.serviceFeeAmount = serviceFeeAmount;
        payment.amountTotal = amountTotal;
        payment.currency = currency;
        payment.paymentMethod = paymentMethod;
        payment.status = status;
        payment.createdAt = createdAt;
        payment.confirmedAt = confirmedAt;
        payment.refundedAt = refundedAt;
        return payment;
    }

    public void confirm(String paymentIntentId, PaymentMethod method) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state");
        }
        this.stripePaymentIntentId = paymentIntentId;
        this.paymentMethod = method;
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public void refund(String refundId) {
        if (this.status != PaymentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed payments can be refunded");
        }
        this.stripeRefundId = refundId;
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state");
        }
        this.status = PaymentStatus.FAILED;
    }
}
