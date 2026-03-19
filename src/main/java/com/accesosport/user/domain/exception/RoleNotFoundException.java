package com.accesosport.user.domain.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String messageKey) {
        super(messageKey);
    }
}
