package com.accesosport.payment.domain.model;

public enum PaymentStatus {
    PENDING,
    CONFIRMED,
    REFUND_PENDING,
    REFUND_FAILED,
    MANUAL_REFUND_PENDING,
    REFUNDED,
    FAILED
}
