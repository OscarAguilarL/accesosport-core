package com.accesosport.auth.infrastructure.persistence.adapter;

import com.accesosport.auth.domain.model.PasswordResetToken;
import com.accesosport.auth.domain.repository.PasswordResetTokenRepository;
import com.accesosport.auth.infrastructure.persistence.jpa.PasswordResetTokenJpaRepository;
import com.accesosport.auth.infrastructure.persistence.mapper.PasswordResetTokenMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(PasswordResetTokenMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(UUID userId) {
        jpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        var entity = PasswordResetTokenMapper.toEntity(token);
        var saved = jpaRepository.save(entity);
        return PasswordResetTokenMapper.toDomain(saved);
    }
}
