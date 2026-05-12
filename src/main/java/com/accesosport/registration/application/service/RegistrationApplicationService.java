package com.accesosport.registration.application.service;

import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.CancelRegistrationCommand;
import com.accesosport.registration.application.dto.GetEventRegistrationsCommand;
import com.accesosport.registration.application.dto.GetMyRegistrationsCommand;
import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.application.dto.RegisterParticipantCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.application.usecase.CancelRegistrationUseCase;
import com.accesosport.registration.application.usecase.GenerateTicketPdfUseCase;
import com.accesosport.registration.application.usecase.GetEventRegistrationsUseCase;
import com.accesosport.registration.application.usecase.GetMyRegistrationsUseCase;
import com.accesosport.registration.application.usecase.GetRegistrationByTicketCodeUseCase;
import com.accesosport.registration.application.usecase.RegisterParticipantUseCase;
import com.accesosport.registration.application.usecase.ResendTicketEmailUseCase;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationApplicationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final ParticipantProfileRepository participantProfileRepository;
    private final UserRepository userRepository;
    private final TicketPdfGenerator ticketPdfGenerator;
    private final EmailService emailService;

    @Transactional
    public RegistrationResponse registerParticipant(UUID eventId, UUID participantId, UUID modalityId, boolean waiverAccepted, boolean wantsShirt) {
        RegisterParticipantUseCase useCase = new RegisterParticipantUseCase(
                registrationRepository, eventRepository, domainEventPublisher, eventModalityRepository, userRepository
        );
        return useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, waiverAccepted, wantsShirt));
    }

    @Transactional
    public RegistrationResponse cancelRegistration(UUID registrationId, UUID requesterId, boolean isAdmin) {
        CancelRegistrationUseCase useCase = new CancelRegistrationUseCase(
                registrationRepository, eventModalityRepository, domainEventPublisher
        );
        return useCase.execute(new CancelRegistrationCommand(registrationId, requesterId, isAdmin));
    }

    @Transactional(readOnly = true)
    public List<ParticipantInEventResponse> getEventRegistrations(UUID eventId) {
        GetEventRegistrationsUseCase useCase = new GetEventRegistrationsUseCase(registrationRepository, participantProfileRepository);
        return useCase.execute(new GetEventRegistrationsCommand(eventId));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(UUID participantId) {
        GetMyRegistrationsUseCase useCase = new GetMyRegistrationsUseCase(registrationRepository, eventRepository);
        return useCase.execute(new GetMyRegistrationsCommand(participantId));
    }

    @Transactional(readOnly = true)
    public ParticipantInEventResponse getRegistrationByTicketCode(String ticketCode) {
        GetRegistrationByTicketCodeUseCase useCase = new GetRegistrationByTicketCodeUseCase(
                registrationRepository, participantProfileRepository
        );
        return useCase.execute(new GetRegistrationByTicketCodeUseCase.Command(ticketCode));
    }

    @Transactional
    public ParticipantInEventResponse markKitPickedUp(String ticketCode) {
        Registration registration = registrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RegistrationNotFoundException(ticketCode));
        registration.markKitPickedUp();
        registrationRepository.save(registration);
        GetRegistrationByTicketCodeUseCase useCase = new GetRegistrationByTicketCodeUseCase(
                registrationRepository, participantProfileRepository
        );
        return useCase.toResponse(registration);
    }

    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(UUID registrationId, UUID requesterId) {
        GenerateTicketPdfUseCase useCase = new GenerateTicketPdfUseCase(
                registrationRepository, eventRepository, userRepository, eventModalityRepository, ticketPdfGenerator
        );
        return useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, requesterId));
    }

    @Transactional(readOnly = true)
    public void resendTicketEmail(UUID registrationId, UUID requesterId) {
        ResendTicketEmailUseCase useCase = new ResendTicketEmailUseCase(
                registrationRepository, eventRepository, userRepository, eventModalityRepository,
                ticketPdfGenerator, emailService
        );
        useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, requesterId));
    }
}
