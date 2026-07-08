package com.accesosport.registration.application.service;

import com.accesosport.registration.domain.model.Registration;

public record ParticipantData(String email, String firstName, String lastName) {

    public static ParticipantData from(Registration r) {
        return new ParticipantData(r.getParticipantEmail(), r.getParticipantFirstName(), r.getParticipantLastName());
    }

    public String fullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? (email != null ? email : "") : full;
    }
}
