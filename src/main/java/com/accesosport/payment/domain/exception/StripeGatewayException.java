package com.accesosport.payment.domain.exception;

public class StripeGatewayException extends RuntimeException {

    public StripeGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
