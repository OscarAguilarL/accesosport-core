package com.accesosport.payment.domain.exception;

public class OrganizerStripeNotLinkedException extends RuntimeException {

    public OrganizerStripeNotLinkedException() {
        super("The organizer has not completed Stripe Connect onboarding");
    }
}
