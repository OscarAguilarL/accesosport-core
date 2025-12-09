package com.grupocaos.products.athletix.event.domain.model;


import java.time.LocalDateTime;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;

public record RegistrationPeriod(LocalDateTime start, LocalDateTime end) {

    public static RegistrationPeriod of(LocalDateTime start, LocalDateTime end) {
        validate(start, end);
        return new RegistrationPeriod(start, end);
    }

    private static void validate(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_PERIOD_START_NOT_NULL);
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_PERIOD_END_BEFORE_START);
        }
    }

    public Boolean isOpen() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(start) && !now.isAfter(end);
    }

    public boolean hasAlreadyClosed() {
        return LocalDateTime.now().isAfter(end);
    }

    public boolean openingSoon() {
        return LocalDateTime.now().isBefore(start);
    }
}
