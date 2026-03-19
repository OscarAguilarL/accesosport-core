package com.accesosport.shared.domain.valueobjects;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public enum VerificationStatus {

    NOT_SUBMITTED,
    PENDING_REVIEW,
    VERIFIED,
    REJECTED;

    private static final EnumMap<VerificationStatus, EnumSet<VerificationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(VerificationStatus.class);

    static {
        // Define transitions here to avoid enum forward-reference/init-order issues.
        ALLOWED_TRANSITIONS.put(NOT_SUBMITTED, EnumSet.of(PENDING_REVIEW));
        ALLOWED_TRANSITIONS.put(PENDING_REVIEW, EnumSet.of(VERIFIED, REJECTED));
        ALLOWED_TRANSITIONS.put(VERIFIED, EnumSet.noneOf(VerificationStatus.class));
        ALLOWED_TRANSITIONS.put(REJECTED, EnumSet.of(PENDING_REVIEW));
    }

    public boolean canTransitionTo(VerificationStatus next) {
        if (next == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }

    public Set<VerificationStatus> getAllowedTransitions() {
        // Expose an unmodifiable view while keeping EnumSet internally.
        return Collections.unmodifiableSet(ALLOWED_TRANSITIONS.get(this));
    }
}