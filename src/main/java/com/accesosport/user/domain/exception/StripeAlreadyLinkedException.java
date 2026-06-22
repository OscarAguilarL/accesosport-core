package com.accesosport.user.domain.exception;

public class StripeAlreadyLinkedException extends RuntimeException {

    public StripeAlreadyLinkedException(String message) {
        super(message);
    }
}
