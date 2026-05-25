package com.accesosport.shared.infrastructure.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final SpringTemplateEngine templateEngine;

    public String registrationConfirmation(
            String participantFirstName,
            String eventName,
            String ticketCode,
            String bibNumber,
            String eventDate,
            String eventLocation
    ) {
        Context ctx = new Context();
        ctx.setVariable("participantFirstName", participantFirstName);
        ctx.setVariable("eventName", eventName);
        ctx.setVariable("ticketCode", ticketCode);
        ctx.setVariable("bibNumber", bibNumber);
        ctx.setVariable("eventDate", eventDate);
        ctx.setVariable("eventLocation", eventLocation);
        return templateEngine.process("email/registration-confirmation", ctx);
    }

    public String eventCancellation(
            String participantFirstName,
            String eventName,
            String eventDate,
            String cancellationReason
    ) {
        Context ctx = new Context();
        ctx.setVariable("participantFirstName", participantFirstName);
        ctx.setVariable("eventName", eventName);
        ctx.setVariable("eventDate", eventDate);
        ctx.setVariable("cancellationReason", cancellationReason);
        return templateEngine.process("email/event-cancellation", ctx);
    }

    public String contactForm(String name, String replyTo, String message) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("replyTo", replyTo);
        ctx.setVariable("message", message);
        return templateEngine.process("email/contact-form", ctx);
    }

    public String eventReminder(
            String participantFirstName,
            String eventName,
            String eventDate,
            String eventLocation,
            String ticketCode,
            String bibNumber
    ) {
        Context ctx = new Context();
        ctx.setVariable("participantFirstName", participantFirstName);
        ctx.setVariable("eventName", eventName);
        ctx.setVariable("eventDate", eventDate);
        ctx.setVariable("eventLocation", eventLocation);
        ctx.setVariable("ticketCode", ticketCode);
        ctx.setVariable("bibNumber", bibNumber);
        return templateEngine.process("email/event-reminder", ctx);
    }
}
