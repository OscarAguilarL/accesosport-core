package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.registration.application.service.TicketPdfGenerator;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplates;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEmailEventHandler {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    private final TicketPdfGenerator ticketPdfGenerator;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventModalityRepository eventModalityRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    public void handle(RegistrationConfirmedEvent event) {
        try {
            Registration registration = registrationRepository.findById(event.getRegistrationId()).orElseThrow(
                    () -> new IllegalStateException("Registration not found: " + event.getRegistrationId())
            );
            Event evt = eventRepository.findById(event.getEventId()).orElseThrow(
                    () -> new IllegalStateException("Event not found: " + event.getEventId())
            );
            User participant = userRepository.findById(event.getParticipantId()).orElse(null);
            if (participant == null) {
                log.warn("[Email] Participant {} not found, skipping ticket email", event.getParticipantId());
                return;
            }

            String distanceLabel = resolveDistanceLabel(registration);

            byte[] pdfBytes = ticketPdfGenerator.generate(registration, evt, participant, distanceLabel);

            String firstName = participant.getPersonalData() != null
                    ? participant.getPersonalData().getFirstName()
                    : "Participante";
            String bibDisplay = event.getBibNumber() != null
                    ? String.valueOf(event.getBibNumber())
                    : "Sin asignar";
            String eventDateStr = evt.getEventDate() != null
                    ? evt.getEventDate().format(DATE_FORMATTER)
                    : "-";
            String location = evt.getLocation() != null
                    ? evt.getLocation().place() + ", " + evt.getLocation().city()
                    : "-";

            String html = EmailTemplates.registrationConfirmation(
                    firstName,
                    evt.getName(),
                    event.getTicketCode(),
                    bibDisplay,
                    eventDateStr,
                    location
            );

            emailService.sendWithAttachment(
                    participant.getEmail(),
                    "Inscripción confirmada — " + evt.getName(),
                    html,
                    "boleto-" + event.getTicketCode() + ".pdf",
                    pdfBytes
            );

            log.info("[Email] Ticket email sent to {} for event {}", participant.getEmail(), event.getEventId());
        } catch (Exception e) {
            log.error("[Email] Failed to send ticket email for registration {}", event.getRegistrationId(), e);
            // No relanzar — la inscripción ya está confirmada en BD
        }
    }

    private String resolveDistanceLabel(Registration registration) {
        if (registration.getModalityId() == null) return null;
        return eventModalityRepository.findById(registration.getModalityId())
                .map(m -> m.getDistance().stripTrailingZeros().toPlainString() + " " + m.getDistanceUnit().getSymbol())
                .orElse(null);
    }
}
