package com.accesosport.payment.domain.exception;

public class InvalidWebhookSignatureException extends RuntimeException {

    public InvalidWebhookSignatureException() {
        super("Invalid Stripe webhook signature");
    }
}
