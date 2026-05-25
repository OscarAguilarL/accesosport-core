package com.accesosport.image.infrastructure.persistence.adapter;

import com.accesosport.image.domain.model.EventImage;
import com.accesosport.image.domain.repository.EventImageRepository;
import com.accesosport.image.infrastructure.persistence.mapper.EventImageMapper;
import com.accesosport.image.infrastructure.persistence.repository.EventImageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventImageRepositoryAdapter implements EventImageRepository {

    private final EventImageJpaRepository jpaRepository;

    @Override
    public EventImage save(EventImage image) {
        return EventImageMapper.toDomain(jpaRepository.save(EventImageMapper.toEntity(image)));
    }

    @Override
    public List<EventImage> findByEventId(UUID eventId) {
        return jpaRepository.findByEventIdOrderByDisplayOrderAsc(eventId).stream()
                .map(EventImageMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<EventImage> findById(UUID id) {
        return jpaRepository.findById(id).map(EventImageMapper::toDomain);
    }

    @Override
    public void delete(EventImage image) {
        jpaRepository.deleteById(image.getId());
    }

    @Override
    public long countByEventId(UUID eventId) {
        return jpaRepository.countByEventId(eventId);
    }
}
