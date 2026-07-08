package com.accesosport.auth.application.usecase;

import com.accesosport.auth.domain.model.PasswordResetToken;
import com.accesosport.auth.domain.repository.PasswordResetTokenRepository;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;
    private final String frontendUrl;

    public void execute(String email) {
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }

        passwordResetTokenRepository.deleteAllByUserId(user.get().getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = PasswordResetToken.create(
                user.get().getId(),
                token,
                LocalDateTime.now().plusHours(1)
        );
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        emailService.send(EmailMessage.of(
                user.get().getEmail(),
                "Recupera tu contraseña — AccesoSport",
                emailTemplatePort.buildPasswordResetEmail(resetLink)
        ));
    }
}
