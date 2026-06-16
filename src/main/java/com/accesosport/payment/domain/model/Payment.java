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
    private int checkoutAttempt;
    private Long version;
    private String refundError;
    private int refundAttempts;

    private Payment() {}

    public static Payment create(
            UUID id,
            UUID registrationId,
            String stripeSessionId,
            BigDecimal baseAmount,
            BigDecimal serviceFeeAmount
    ) {
        Payment payment = new Payment();
        payment.id = id;
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
        payment.checkoutAttempt = 1;
        payment.version = null;
        payment.refundError = null;
        payment.refundAttempts = 0;
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
            Instant refundedAt,
            int checkoutAttempt,
            Long version,
            String refundError,
            int refundAttempts
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
        payment.checkoutAttempt = checkoutAttempt;
        payment.version = version;
        payment.refundError = refundError;
        payment.refundAttempts = refundAttempts;
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

    public void incrementCheckoutAttempt() {
        this.checkoutAttempt++;
    }

    public void updateStripeSessionId(String newSessionId) {
        this.stripeSessionId = newSessionId;
    }

    public void initiateRefund() {
        if (this.status != PaymentStatus.CONFIRMED && this.status != PaymentStatus.REFUND_FAILED) {
            throw new IllegalStateException("Only confirmed or failed-refund payments can be refunded");
        }
        this.status = PaymentStatus.REFUND_PENDING;
        this.refundError = null;
    }

    public void completeRefund(String refundId) {
        if (this.status != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Payment is not in REFUND_PENDING state");
        }
        this.stripeRefundId = refundId;
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }

    public void failRefund(String error) {
        if (this.status != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Payment is not in REFUND_PENDING state");
        }
        this.refundError = error;
        this.refundAttempts++;
        this.status = PaymentStatus.REFUND_FAILED;
    }

    public void initiateManualRefund() {
        if (this.status != PaymentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed payments can be marked for manual refund");
        }
        this.status = PaymentStatus.MANUAL_REFUND_PENDING;
    }

    public void completeManualRefund() {
        if (this.status != PaymentStatus.MANUAL_REFUND_PENDING) {
            throw new IllegalStateException("Payment is not in MANUAL_REFUND_PENDING state");
        }
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
