package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.GetMyRegistrationsCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GetMyRegistrationsUseCase extends UseCase<GetMyRegistrationsCommand, List<RegistrationResponse>> {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    @Override
    protected List<RegistrationResponse> internalExecute(GetMyRegistrationsCommand command) {
        List<Registration> registrations = registrationRepository.findByParticipantId(command.participantId());

        List<UUID> eventIds = registrations.stream()
                .map(Registration::getEventId)
                .distinct()
                .toList();

        Map<UUID, Event> eventMap = eventIds.stream()
                .map(eventRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(Event::getId, e -> e));

        return registrations.stream()
                .map(r -> {
                    Event event = eventMap.get(r.getEventId());
                    if (event != null) {
                        return RegistrationResponse.from(r, event.getName(), event.getEventDate());
                    }
                    return RegistrationResponse.from(r);
                })
                .toList();
    }
}
