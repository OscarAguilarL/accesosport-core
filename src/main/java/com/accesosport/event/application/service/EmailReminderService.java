package com.accesosport.event.application.service;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplates;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailReminderService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findEventsNeedingReminder(now, now.plusHours(25));
        log.info("[Reminder] Found {} event(s) needing reminder", events.size());

        for (Event event : events) {
            try {
                event.setReminderSentAt(LocalDateTime.now());
                eventRepository.save(event);

                List<Registration> confirmed = registrationRepository.findConfirmedByEventId(event.getId());
                log.info("[Reminder] Sending reminder for event '{}' to {} participant(s)",
                        event.getName(), confirmed.size());

                for (Registration reg : confirmed) {
                    try {
                        userRepository.findById(reg.getParticipantId()).ifPresent(user -> {
                            String firstName = user.getPersonalData() != null
                                    ? user.getPersonalData().getFirstName()
                                    : "Participant";
                            String html = EmailTemplates.eventReminder(
                                    firstName,
                                    event.getName(),
                                    event.getEventDate().format(DATE_FORMATTER),
                                    event.getLocation().place() + ", " + event.getLocation().city(),
                                    reg.getTicketCode(),
                                    reg.getBibNumber() != null ? String.valueOf(reg.getBibNumber()) : "Pending"
                            );
                            emailService.send(EmailMessage.of(
                                    user.getEmail(),
                                    "Race Day Tomorrow: " + event.getName(),
                                    html
                            ));
                        });
                    } catch (Exception e) {
                        log.error("[Reminder] Failed to send reminder to participant {} for event {}",
                                reg.getParticipantId(), event.getId(), e);
                    }
                }
            } catch (Exception e) {
                log.error("[Reminder] Failed to process reminder for event {}", event.getId(), e);
            }
        }
    }
}
