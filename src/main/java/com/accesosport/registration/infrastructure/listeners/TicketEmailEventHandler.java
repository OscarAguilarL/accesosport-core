package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCategory;
import com.accesosport.event.domain.repository.EventCategoryRepository;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.service.ParticipantData;
import com.accesosport.registration.application.service.TicketPdfGenerator;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEmailEventHandler {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, h:mm a").withLocale(java.util.Locale.forLanguageTag("es-MX"));

    private final TicketPdfGenerator ticketPdfGenerator;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventModalityRepository eventModalityRepository;
    private final EventCategoryRepository eventCategoryRepository;

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

            ParticipantData participant = ParticipantData.from(registration);

            if (participant.email() == null || participant.email().isBlank()) {
                log.warn("[Email] No participant email for registration {}, skipping ticket email", event.getRegistrationId());
                return;
            }

            String distanceLabel = resolveDistanceLabel(registration);

            String category = null;
            if (registration.getCategoryId() != null) {
                category = eventCategoryRepository.findById(registration.getCategoryId())
                        .map(EventCategory::getName)
                        .orElse(null);
            }

            byte[] pdfBytes = ticketPdfGenerator.generate(registration, evt, participant, distanceLabel, category, registration.isWantsShirt());

            String firstName = participant.firstName() != null ? participant.firstName() : "Participante";
            String bibDisplay = event.getBibNumber() != null
                    ? String.valueOf(event.getBibNumber())
                    : "Sin asignar";
            String eventDateStr = evt.getEventDate() != null
                    ? evt.getEventDate().format(DATE_FORMATTER)
                    : "-";
            String location = evt.getLocation() != null
                    ? evt.getLocation().place() + ", " + evt.getLocation().city()
                    : "-";

            String html = emailTemplatePort.registrationConfirmation(
                    firstName,
                    evt.getName(),
                    event.getTicketCode(),
                    bibDisplay,
                    eventDateStr,
                    location
            );

            emailService.sendWithAttachment(
                    participant.email(),
                    "Inscripción confirmada — " + evt.getName(),
                    html,
                    "boleto-" + event.getTicketCode() + ".pdf",
                    pdfBytes
            );

            log.info("[Email] Ticket email sent to {} for event {}", participant.email(), event.getEventId());
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
