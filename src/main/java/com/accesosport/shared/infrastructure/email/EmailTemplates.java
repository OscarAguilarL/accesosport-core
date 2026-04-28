package com.accesosport.shared.infrastructure.email;

public final class EmailTemplates {

    private EmailTemplates() {}

    public static String registrationConfirmation(
            String participantFirstName,
            String eventName,
            String ticketCode,
            String bibNumber,
            String eventDate,
            String eventLocation
    ) {
        return """
                <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; color: #111827;">
                  <h2 style="color: #2563eb;">Registration Confirmed — %s</h2>
                  <p>Hello %s,</p>
                  <p>You are officially registered for <strong>%s</strong>.</p>
                  <table style="border-collapse: collapse; width: 100%%;">
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Ticket Code</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Bib Number</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Date</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Location</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                  </table>
                  <p style="margin-top: 24px;">See you at the race!</p>
                  <p style="color: #6b7280; font-size: 12px;">AccesoSport — Your race, your moment.</p>
                </body></html>
                """.formatted(eventName, participantFirstName, eventName, ticketCode, bibNumber, eventDate, eventLocation);
    }

    public static String eventCancellation(
            String participantFirstName,
            String eventName,
            String eventDate,
            String cancellationReason
    ) {
        return """
                <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; color: #111827;">
                  <h2 style="color: #dc2626;">Event Cancelled — %s</h2>
                  <p>Hello %s,</p>
                  <p>We regret to inform you that <strong>%s</strong> scheduled for <strong>%s</strong> has been cancelled.</p>
                  <p><strong>Reason:</strong> %s</p>
                  <p>If you made a payment, a refund will be processed automatically within 3–5 business days.</p>
                  <p style="color: #6b7280; font-size: 12px;">AccesoSport — We apologize for the inconvenience.</p>
                </body></html>
                """.formatted(eventName, participantFirstName, eventName, eventDate, cancellationReason);
    }

    public static String eventReminder(
            String participantFirstName,
            String eventName,
            String eventDate,
            String eventLocation,
            String ticketCode,
            String bibNumber
    ) {
        return """
                <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; color: #111827;">
                  <h2 style="color: #16a34a;">Race Day Tomorrow — %s</h2>
                  <p>Hello %s,</p>
                  <p>Your race starts tomorrow! Here are your details:</p>
                  <table style="border-collapse: collapse; width: 100%%;">
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Event</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Date &amp; Time</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Location</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Ticket Code</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #e5e7eb;"><strong>Bib Number</strong></td>
                        <td style="padding: 8px; border: 1px solid #e5e7eb;">%s</td></tr>
                  </table>
                  <p style="margin-top: 24px;">Good luck and have a great race!</p>
                  <p style="color: #6b7280; font-size: 12px;">AccesoSport — Your race, your moment.</p>
                </body></html>
                """.formatted(eventName, participantFirstName, eventName, eventDate, eventLocation, ticketCode, bibNumber);
    }
}
