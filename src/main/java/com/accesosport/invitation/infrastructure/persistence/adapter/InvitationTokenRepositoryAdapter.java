package com.accesosport.invitation.infrastructure.persistence.adapter;

import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.domain.repository.InvitationTokenRepository;
import com.accesosport.invitation.infrastructure.persistence.jpa.InvitationTokenJpaRepository;
import com.accesosport.invitation.infrastructure.persistence.mapper.InvitationTokenMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class InvitationTokenRepositoryAdapter implements InvitationTokenRepository {

    private final InvitationTokenJpaRepository jpaRepository;

    @Override
    public Optional<InvitationToken> findByToken(UUID token) {
        return jpaRepository.findByToken(token)
                .map(InvitationTokenMapper::toDomain);
    }

    @Override
    public List<InvitationToken> findAll() {
        return jpaRepository.findAll().stream()
                .map(InvitationTokenMapper::toDomain)
                .toList();
    }

    @Override
    public InvitationToken save(InvitationToken token) {
        var entity = InvitationTokenMapper.toEntity(token);
        var saved = jpaRepository.save(entity);
        return InvitationTokenMapper.toDomain(saved);
    }
}
