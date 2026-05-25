package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CategoryResponse;
import com.accesosport.event.application.dto.CreateCategoryRequest;
import com.accesosport.event.domain.exception.CategoryNotFoundException;
import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCategory;
import com.accesosport.event.domain.repository.EventCategoryRepository;
import com.accesosport.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventCategoryApplicationService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse addCategory(UUID eventId, UUID requesterId, boolean isAdmin, CreateCategoryRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && requesterId != null && !event.getCreatedBy().getId().equals(requesterId)) {
            throw new EventAccessDeniedException();
        }

        EventCategory category = EventCategory.create(
                eventId,
                request.modalityId(),
                request.name(),
                request.minAge(),
                request.maxAge()
        );

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(UUID eventId) {
        return categoryRepository.findByEventId(eventId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public void deleteCategory(UUID eventId, UUID categoryId, UUID requesterId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && requesterId != null && !event.getCreatedBy().getId().equals(requesterId)) {
            throw new EventAccessDeniedException();
        }

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        categoryRepository.deleteById(categoryId);
    }
}
