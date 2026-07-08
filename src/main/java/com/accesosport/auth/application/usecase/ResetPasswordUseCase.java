package com.accesosport.auth.application.usecase;

import com.accesosport.auth.domain.exception.InvalidTokenException;
import com.accesosport.auth.domain.exception.TokenAlreadyUsedException;
import com.accesosport.auth.domain.exception.TokenExpiredException;
import com.accesosport.auth.domain.model.PasswordResetToken;
import com.accesosport.auth.domain.repository.PasswordResetTokenRepository;
import com.accesosport.auth.domain.service.PasswordEncoder;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void execute(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException(MessageKeys.PasswordReset.INVALID_TOKEN));

        if (resetToken.isUsed()) {
            throw new TokenAlreadyUsedException(MessageKeys.PasswordReset.TOKEN_ALREADY_USED);
        }

        if (resetToken.isExpired()) {
            throw new TokenExpiredException(MessageKeys.PasswordReset.TOKEN_EXPIRED);
        }

        var user = userRepository.findById(resetToken.getUserId()).orElseThrow();
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
    }
}
