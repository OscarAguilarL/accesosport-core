package com.accesosport.invitation.infrastructure.persistence.jpa;

import com.accesosport.invitation.infrastructure.persistence.entity.InvitationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationTokenJpaRepository extends JpaRepository<InvitationTokenJpaEntity, UUID> {
    Optional<InvitationTokenJpaEntity> findByToken(UUID token);
}
