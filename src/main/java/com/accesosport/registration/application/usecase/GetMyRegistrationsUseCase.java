package com.accesosport.registration.application.usecase;

import com.accesosport.registration.application.dto.GetMyRegistrationsCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetMyRegistrationsUseCase extends UseCase<GetMyRegistrationsCommand, List<RegistrationResponse>> {

    private final RegistrationRepository registrationRepository;

    @Override
    protected List<RegistrationResponse> internalExecute(GetMyRegistrationsCommand command) {
        return registrationRepository.findByParticipantId(command.participantId()).stream()
                .map(RegistrationResponse::from)
                .toList();
    }
}
