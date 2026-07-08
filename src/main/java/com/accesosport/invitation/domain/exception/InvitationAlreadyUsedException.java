package com.accesosport.invitation.domain.exception;

public class InvitationAlreadyUsedException extends RuntimeException {
    public InvitationAlreadyUsedException(String message) {
        super(message);
    }
}
