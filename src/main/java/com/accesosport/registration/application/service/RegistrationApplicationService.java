package com.accesosport.registration.application.service;

import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.CancelRegistrationCommand;
import com.accesosport.registration.application.dto.GetEventRegistrationsCommand;
import com.accesosport.registration.application.dto.GetMyRegistrationsCommand;
import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.application.dto.RegisterParticipantCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.application.usecase.CancelRegistrationUseCase;
import com.accesosport.registration.application.usecase.GetEventRegistrationsUseCase;
import com.accesosport.registration.application.usecase.GetMyRegistrationsUseCase;
import com.accesosport.registration.application.usecase.RegisterParticipantUseCase;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
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
    private final EventCapacityRepository eventCapacityRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public RegistrationResponse registerParticipant(UUID eventId, UUID participantId) {
        RegisterParticipantUseCase useCase = new RegisterParticipantUseCase(
                registrationRepository, eventRepository, eventCapacityRepository, domainEventPublisher
        );
        return useCase.execute(new RegisterParticipantCommand(eventId, participantId));
    }

    @Transactional
    public RegistrationResponse cancelRegistration(UUID registrationId, UUID requesterId, boolean isAdmin) {
        CancelRegistrationUseCase useCase = new CancelRegistrationUseCase(
                registrationRepository, eventCapacityRepository, domainEventPublisher
        );
        return useCase.execute(new CancelRegistrationCommand(registrationId, requesterId, isAdmin));
    }

    @Transactional(readOnly = true)
    public List<ParticipantInEventResponse> getEventRegistrations(UUID eventId) {
        GetEventRegistrationsUseCase useCase = new GetEventRegistrationsUseCase(registrationRepository);
        return useCase.execute(new GetEventRegistrationsCommand(eventId));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(UUID participantId) {
        GetMyRegistrationsUseCase useCase = new GetMyRegistrationsUseCase(registrationRepository);
        return useCase.execute(new GetMyRegistrationsCommand(participantId));
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationByTicketCode(String ticketCode) {
        Registration registration = registrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RegistrationNotFoundException(ticketCode));
        return RegistrationResponse.from(registration);
    }

    @Transactional
    public RegistrationResponse markKitPickedUp(String ticketCode) {
        Registration registration = registrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RegistrationNotFoundException(ticketCode));
        registration.markKitPickedUp();
        registrationRepository.save(registration);
        return RegistrationResponse.from(registration);
    }
}
