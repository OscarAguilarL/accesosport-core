package com.grupocaos.products.athletix.event.domain.model;


import java.time.LocalDateTime;

public record RegistrationPeriod(LocalDateTime start, LocalDateTime end) {

    public RegistrationPeriod of(LocalDateTime start, LocalDateTime end) {
        validate(start, end);
        return new RegistrationPeriod(start, end);
    }

    private static void validate(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end cannot be null");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must be before start");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
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
