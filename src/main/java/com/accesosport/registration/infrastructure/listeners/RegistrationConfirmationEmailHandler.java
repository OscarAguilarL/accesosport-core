package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplates;
import com.accesosport.user.domain.repository.UserRepository;
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
public class RegistrationConfirmationEmailHandler {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    public void handle(RegistrationConfirmedEvent event) {
        try {
            userRepository.findById(event.getParticipantId()).ifPresent(user -> {
                Event domainEvent = eventRepository.findById(event.getEventId())
                        .orElseThrow(() -> new IllegalStateException("Event not found: " + event.getEventId()));

                String firstName = user.getPersonalData() != null
                        ? user.getPersonalData().getFirstName()
                        : "Participant";

                String html = EmailTemplates.registrationConfirmation(
                        firstName,
                        domainEvent.getName(),
                        event.getTicketCode(),
                        event.getBibNumber() != null ? String.valueOf(event.getBibNumber()) : "Pending assignment",
                        domainEvent.getEventDate().format(DATE_FORMATTER),
                        domainEvent.getLocation().place() + ", " + domainEvent.getLocation().city()
                );

                emailService.send(EmailMessage.of(
                        user.getEmail(),
                        "Registration Confirmed: " + domainEvent.getName(),
                        html
                ));
                log.info("[Email] Registration confirmation sent to {} for event {}",
                        user.getEmail(), event.getEventId());
            });
        } catch (Exception e) {
            log.error("[Email] Registration confirmation failed for registration {}",
                    event.getRegistrationId(), e);
        }
    }
}
