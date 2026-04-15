package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.ParticipantInEventResponse;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class GetEventRegistrationsUseCase extends UseCase<GetEventRegistrationsUseCase.GetEventRegistrationsCommand, List<ParticipantInEventResponse>> {

    private final RegistrationRepository registrationRepository;

    @Override
    protected List<ParticipantInEventResponse> internalExecute(GetEventRegistrationsCommand command) {
        return registrationRepository.findByEventId(command.eventId()).stream()
                .map(r -> new ParticipantInEventResponse(
                        r.getId(),
                        r.getParticipantId(),
                        r.getStatus().name(),
                        r.getTicketCode(),
                        r.getBibNumber(),
                        r.isKitPickedUp(),
                        r.getRegisteredAt()
                ))
                .toList();
    }

    public record GetEventRegistrationsCommand(UUID eventId) {}
}
