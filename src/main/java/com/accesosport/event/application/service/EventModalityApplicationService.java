package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CreateModalityRequest;
import com.accesosport.event.application.dto.ModalityResponse;
import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.exception.ModalityHasRegistrationsException;
import com.accesosport.event.domain.exception.ModalityNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventModalityApplicationService {

    private final EventRepository eventRepository;
    private final EventModalityRepository modalityRepository;

    @Transactional
    public ModalityResponse addModality(UUID eventId, UUID requesterId, boolean isAdmin, CreateModalityRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && requesterId != null && !event.getCreatedBy().getId().equals(requesterId)) {
            throw new EventAccessDeniedException();
        }

        EventModality modality = EventModality.create(
                eventId,
                request.name(),
                request.distance(),
                request.distanceUnit(),
                request.price(),
                request.priceWithoutShirt(),
                request.capacity()
        );

        return ModalityResponse.from(modalityRepository.save(modality));
    }

    @Transactional(readOnly = true)
    public List<ModalityResponse> listModalities(UUID eventId) {
        return modalityRepository.findByEventId(eventId).stream()
                .map(ModalityResponse::from)
                .toList();
    }

    @Transactional
    public void deleteModality(UUID eventId, UUID modalityId, UUID requesterId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && requesterId != null && !event.getCreatedBy().getId().equals(requesterId)) {
            throw new EventAccessDeniedException();
        }

        EventModality modality = modalityRepository.findById(modalityId)
                .orElseThrow(() -> new ModalityNotFoundException(modalityId));

        if (modality.hasRegistrations()) {
            throw new ModalityHasRegistrationsException(modalityId);
        }

        modalityRepository.deleteById(modalityId);
    }
}
