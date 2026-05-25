package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCategory;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventCategoryRepository;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.service.TicketPdfGenerator;
import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.exception.RegistrationNotConfirmedException;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class GenerateTicketPdfUseCase extends UseCase<GenerateTicketPdfUseCase.Command, byte[]> {

    public record Command(UUID registrationId, UUID requesterId) {}

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventModalityRepository eventModalityRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final TicketPdfGenerator ticketPdfGenerator;

    @Override
    protected byte[] internalExecute(Command command) {
        Registration registration = registrationRepository.findById(command.registrationId())
                .orElseThrow(() -> new RegistrationNotFoundException(command.registrationId()));

        if (!registration.getParticipantId().equals(command.requesterId())) {
            throw new RegistrationAccessDeniedException(command.registrationId(), command.requesterId());
        }

        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new RegistrationNotConfirmedException(command.registrationId());
        }

        Event event = eventRepository.findById(registration.getEventId())
                .orElseThrow(() -> new IllegalStateException("Event not found for registration: " + command.registrationId()));

        User participant = userRepository.findById(command.requesterId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + command.requesterId()));

        String distanceLabel = null;
        if (registration.getModalityId() != null) {
            distanceLabel = eventModalityRepository.findById(registration.getModalityId())
                    .map(m -> m.getDistance().stripTrailingZeros().toPlainString() + " " + m.getDistanceUnit().getSymbol())
                    .orElse(null);
        }
        
        String category = null;
        if (registration.getCategoryId() != null) {
        	category = eventCategoryRepository.findById(registration.getCategoryId())
        			.map(EventCategory::getName)
        			.orElse(null);
        }

        try {
            return ticketPdfGenerator.generate(registration, event, participant, distanceLabel, category, registration.isWantsShirt());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el boleto PDF", e);
        }
    }
}
