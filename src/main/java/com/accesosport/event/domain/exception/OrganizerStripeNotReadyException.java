package com.accesosport.event.domain.exception;

public class OrganizerStripeNotReadyException extends RuntimeException {
    public OrganizerStripeNotReadyException() {
        super("El organizador debe tener una cuenta de Stripe Connect activa para crear eventos de pago");
    }
}
