package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class GetMyRegistrationsUseCase extends UseCase<GetMyRegistrationsUseCase.GetMyRegistrationsCommand, List<RegistrationResponse>> {

    private final RegistrationRepository registrationRepository;

    @Override
    protected List<RegistrationResponse> internalExecute(GetMyRegistrationsCommand command) {
        return registrationRepository.findByParticipantId(command.participantId()).stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    public record GetMyRegistrationsCommand(UUID participantId) {}
}
