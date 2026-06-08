package com.accesosport.shared.domain.port;

public interface EmailTemplatePort {
    String registrationConfirmation(
            String participantFirstName,
            String eventName,
            String ticketCode,
            String bibNumber,
            String eventDate,
            String eventLocation
    );

    String eventCancellation(
            String participantFirstName,
            String eventName,
            String eventDate,
            String cancellationReason
    );

    String contactForm(String name, String replyTo, String message);

    String eventReminder(
            String participantFirstName,
            String eventName,
            String eventDate,
            String eventLocation,
            String ticketCode,
            String bibNumber
    );

    String buildPasswordResetEmail(String resetLink);

    String buildOrganizerInvitationEmail(String reason, String invitationLink);
}
