package com.accesosport.payment.domain.exception;

public class OxxoRefundNotSupportedException extends RuntimeException {

    public OxxoRefundNotSupportedException() {
        super("OXXO payments cannot be refunded automatically. Please process the refund manually.");
    }
}
