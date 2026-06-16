package com.accesosport.event.domain.exception;

public class OrganizerNotVerifiedException extends RuntimeException {
    public OrganizerNotVerifiedException() {
        super("El organizador debe estar verificado para crear eventos de pago");
    }
}
