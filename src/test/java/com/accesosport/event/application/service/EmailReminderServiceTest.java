package com.accesosport.event.application.service;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplateService;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailReminderServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private EmailTemplateService emailTemplateService;

    @Mock private Event event;
    @Mock private Location location;
    @Mock private User user;
    @Mock private Registration registration;

    private EmailReminderService service;
    private UUID eventId;
    private UUID participantId;

    @BeforeEach
    void setUp() {
        service = new EmailReminderService(eventRepository, registrationRepository, userRepository, emailService, emailTemplateService);
        eventId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Carrera 10K");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        when(location.place()).thenReturn("Chapultepec");
        when(location.city()).thenReturn("CDMX");

        when(registration.getParticipantId()).thenReturn(participantId);
        when(registration.getTicketCode()).thenReturn("ACSP-5555");
        when(registration.getBibNumber()).thenReturn(42);

        when(user.getEmail()).thenReturn("runner@test.com");
        when(user.getPersonalData()).thenReturn(PersonalData.builder().firstName("Carlos").build());
        when(userRepository.findById(participantId)).thenReturn(Optional.of(user));

        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(emailTemplateService.eventReminder(any(), any(), any(), any(), any(), any())).thenReturn("<html>stub</html>");
    }

    @Test
    void sendEventReminders_shouldSetReminderSentAtBeforeSendingEmail() {
        when(eventRepository.findEventsNeedingReminder(any(), any())).thenReturn(List.of(event));
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of(registration));

        service.sendEventReminders();

        InOrder order = inOrder(event, eventRepository, emailService);
        order.verify(event).setReminderSentAt(any(LocalDateTime.class));
        order.verify(eventRepository).save(event);
        order.verify(emailService).send(any(EmailMessage.class));
    }

    @Test
    void sendEventReminders_shouldSendOneEmailPerConfirmedParticipant() {
        Registration reg2 = mock(Registration.class);
        UUID p2 = UUID.randomUUID();
        when(reg2.getParticipantId()).thenReturn(p2);
        when(reg2.getTicketCode()).thenReturn("ACSP-6666");
        when(reg2.getBibNumber()).thenReturn(43);
        User user2 = mock(User.class);
        when(user2.getEmail()).thenReturn("runner2@test.com");
        when(user2.getPersonalData()).thenReturn(PersonalData.builder().firstName("Maria").build());
        when(userRepository.findById(p2)).thenReturn(Optional.of(user2));

        when(eventRepository.findEventsNeedingReminder(any(), any())).thenReturn(List.of(event));
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of(registration, reg2));

        service.sendEventReminders();

        verify(emailService, times(2)).send(any(EmailMessage.class));
    }

    @Test
    void sendEventReminders_whenNoEventsNeedReminder_shouldSendNothing() {
        when(eventRepository.findEventsNeedingReminder(any(), any())).thenReturn(List.of());

        service.sendEventReminders();

        verify(emailService, never()).send(any());
        verify(registrationRepository, never()).findConfirmedByEventId(any());
    }

    @Test
    void sendEventReminders_whenSendFails_shouldContinueWithNextParticipant() {
        Registration reg2 = mock(Registration.class);
        UUID p2 = UUID.randomUUID();
        when(reg2.getParticipantId()).thenReturn(p2);
        when(reg2.getTicketCode()).thenReturn("ACSP-7777");
        when(reg2.getBibNumber()).thenReturn(99);
        User user2 = mock(User.class);
        when(user2.getEmail()).thenReturn("runner2@test.com");
        when(user2.getPersonalData()).thenReturn(PersonalData.builder().firstName("Sofía").build());
        when(userRepository.findById(p2)).thenReturn(Optional.of(user2));

        when(eventRepository.findEventsNeedingReminder(any(), any())).thenReturn(List.of(event));
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of(registration, reg2));
        doThrow(new RuntimeException("API error")).doNothing().when(emailService).send(any());

        assertThatNoException().isThrownBy(() -> service.sendEventReminders());
        verify(emailService, times(2)).send(any());
    }

    @Test
    void sendEventReminders_shouldQueryWithCorrectTimeWindow() {
        when(eventRepository.findEventsNeedingReminder(any(), any())).thenReturn(List.of());

        service.sendEventReminders();

        // Verifies the query uses a 25-hour window from now
        verify(eventRepository).findEventsNeedingReminder(
                any(LocalDateTime.class),
                argThat(to -> to.isAfter(LocalDateTime.now().plusHours(24)))
        );
    }
}
