package com.accesosport.auth.domain.exception;

import com.accesosport.shared.domain.i18n.MessageKeys;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super(MessageKeys.AuthMessages.INVALID_CURRENT_PASSWORD);
    }
}
