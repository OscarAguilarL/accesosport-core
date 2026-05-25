package com.accesosport.registration.infrastructure.persistence.jpa;

import com.accesosport.registration.infrastructure.persistence.entity.CheckinTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckinTokenJpaRepository extends JpaRepository<CheckinTokenJpaEntity, UUID> {

    Optional<CheckinTokenJpaEntity> findByToken(String token);
}
