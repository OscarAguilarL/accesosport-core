package com.accesosport.user.domain.exception;

public class PersonalDataNotFoundException extends RuntimeException {

    public PersonalDataNotFoundException(String messageKey) {
        super(messageKey);
    }
}
