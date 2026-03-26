package com.accesosport.event.domain.exception;

import com.accesosport.shared.domain.i18n.MessageKeys;

public class EventAccessDeniedException extends RuntimeException {

    public EventAccessDeniedException() {
        super(MessageKeys.Events.EVENT_ACCESS_DENIED);
    }
}
