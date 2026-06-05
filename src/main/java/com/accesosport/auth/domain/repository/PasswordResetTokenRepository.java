package com.accesosport.auth.domain.repository;

import com.accesosport.auth.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByUserId(UUID userId);
    PasswordResetToken save(PasswordResetToken token);
}
