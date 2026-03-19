package com.accesosport.event.domain.model;

public enum EventStatus {
    DRAFT,                  // Event created but not yet published
    PUBLISHED,              // Event is visible but registration is closed
    REGISTRATION_OPEN,      // Accepting registrations
    REGISTRATION_CLOSED,    // No more registrations are accepted
    IN_PROGRESS,            // Event is in progress
    COMPLETED,              // Event has finished
    CANCELLED;               // Event has been cancelled

    public boolean canBePublished() {
        return this == DRAFT;
    }

    public boolean canOpenRegistration() {
        return this == PUBLISHED;
    }

    public boolean canBeCancelled() {
        return this == PUBLISHED || this == COMPLETED || this == REGISTRATION_OPEN;
    }

    public boolean acceptsRegistrations() {
        return this == REGISTRATION_OPEN;
    }
}
