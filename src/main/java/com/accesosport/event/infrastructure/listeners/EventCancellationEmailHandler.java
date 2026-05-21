package com.accesosport.event.infrastructure.listeners;

import com.accesosport.event.domain.events.EventCancelledEvent;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplateService;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCancellationEmailHandler {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, h:mm a").withLocale(java.util.Locale.forLanguageTag("es-MX"));

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("domainEventExecutor")
    public void handle(EventCancelledEvent event) {
        try {
            log.info("[Email] Processing cancellation emails for event '{}' ({} affected registration(s))",
                    event.getEventName(), event.getAffectedRegistrationIds().size());

            for (UUID registrationId : event.getAffectedRegistrationIds()) {
                try {
                    registrationRepository.findById(registrationId).ifPresent(reg ->
                            userRepository.findById(reg.getParticipantId()).ifPresent(user -> {
                                String firstName = user.getPersonalData() != null
                                        ? user.getPersonalData().getFirstName()
                                        : "Participante";

                                String html = emailTemplateService.eventCancellation(
                                        firstName,
                                        event.getEventName(),
                                        event.getEventDate().format(DATE_FORMATTER),
                                        event.getCancellationReason()
                                );

                                emailService.send(EmailMessage.of(
                                        user.getEmail(),
                                        "Importante: " + event.getEventName() + " ha sido cancelado",
                                        html
                                ));
                                log.info("[Email] Cancellation notice sent to {} for event {}",
                                        user.getEmail(), event.getEventId());
                            })
                    );
                } catch (Exception e) {
                    log.error("[Email] Cancellation email failed for registration {}", registrationId, e);
                }
            }
        } catch (Exception e) {
            log.error("[Email] Event cancellation email batch failed for event {}", event.getEventId(), e);
        }
    }
}
