package com.accesosport.registration.domain.model;

public enum PaymentMethod {
    CARD,
    OXXO,
    CASH_OTHER,
    OTHER;

    public boolean requiresExtendedExpiry() {
        return this == OXXO || this == CASH_OTHER;
    }
}
