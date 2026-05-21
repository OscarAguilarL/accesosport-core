package com.accesosport.event.infrastructure.listeners;

import com.accesosport.event.domain.events.EventCancelledEvent;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCancellationEmailHandlerTest {

    @Mock private EmailService emailService;
    @Mock private EmailTemplateService emailTemplateService;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private UserRepository userRepository;

    private EventCancellationEmailHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EventCancellationEmailHandler(emailService, emailTemplateService, registrationRepository, userRepository);
        when(emailTemplateService.eventCancellation(any(), any(), any(), any())).thenReturn("<html>stub</html>");
    }

    private EventCancelledEvent buildEvent(List<UUID> registrationIds) {
        com.accesosport.event.domain.model.Event mockEvent = mock(com.accesosport.event.domain.model.Event.class);
        when(mockEvent.getId()).thenReturn(UUID.randomUUID());
        when(mockEvent.getName()).thenReturn("Carrera 10K");
        when(mockEvent.getEventDate()).thenReturn(LocalDateTime.of(2026, 7, 1, 7, 0));
        return new EventCancelledEvent(mockEvent, "Condiciones climáticas", registrationIds);
    }

    private Registration stubRegistration(UUID registrationId, UUID participantId) {
        Registration reg = mock(Registration.class);
        when(reg.getParticipantId()).thenReturn(participantId);
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(reg));
        return reg;
    }

    private User stubUser(UUID participantId, String email) {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        when(user.getPersonalData()).thenReturn(PersonalData.builder().firstName("Luis").build());
        when(userRepository.findById(participantId)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void handle_shouldSendEmailToAllAffectedParticipants() {
        UUID reg1 = UUID.randomUUID(), reg2 = UUID.randomUUID(), reg3 = UUID.randomUUID();
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        stubRegistration(reg1, p1);
        stubRegistration(reg2, p2);
        stubRegistration(reg3, p3);
        stubUser(p1, "p1@test.com");
        stubUser(p2, "p2@test.com");
        stubUser(p3, "p3@test.com");

        handler.handle(buildEvent(List.of(reg1, reg2, reg3)));

        verify(emailService, times(3)).send(any(EmailMessage.class));
    }

    @Test
    void handle_whenOneParticipantEmailFails_shouldContinueWithRest() {
        UUID reg1 = UUID.randomUUID(), reg2 = UUID.randomUUID();
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        stubRegistration(reg1, p1);
        stubRegistration(reg2, p2);
        stubUser(p1, "p1@test.com");
        stubUser(p2, "p2@test.com");

        doThrow(new RuntimeException("Send failed")).doNothing()
                .when(emailService).send(any());

        assertThatNoException().isThrownBy(() -> handler.handle(buildEvent(List.of(reg1, reg2))));
        verify(emailService, times(2)).send(any());
    }

    @Test
    void handle_whenRegistrationNotFound_shouldSkipAndContinue() {
        UUID reg1 = UUID.randomUUID(), reg2 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();

        when(registrationRepository.findById(reg1)).thenReturn(Optional.empty());
        stubRegistration(reg2, p2);
        stubUser(p2, "p2@test.com");

        handler.handle(buildEvent(List.of(reg1, reg2)));

        verify(emailService, times(1)).send(any());
    }
}
