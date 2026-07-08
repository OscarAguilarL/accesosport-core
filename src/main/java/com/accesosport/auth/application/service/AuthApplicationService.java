package com.accesosport.auth.application.service;

import com.accesosport.auth.application.dto.AuthResponse;
import com.accesosport.auth.application.dto.AuthResponseMapper;
import com.accesosport.auth.application.dto.LoginRequest;
import com.accesosport.auth.application.dto.RegisterRequest;
import com.accesosport.auth.application.usecase.ChangePasswordUseCase;
import com.accesosport.auth.application.usecase.RequestPasswordResetUseCase;
import com.accesosport.auth.application.usecase.ResetPasswordUseCase;
import com.accesosport.auth.domain.repository.PasswordResetTokenRepository;
import com.accesosport.auth.domain.service.AuthenticationService;
import com.accesosport.auth.domain.service.PasswordEncoder;
import com.accesosport.auth.domain.service.TokenProvider;
import com.accesosport.auth.application.usecase.AuthenticateUserUseCase;
import com.accesosport.auth.application.usecase.RegisterUserUseCase;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.domain.repository.UserRepository;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationService authenticationService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        AuthenticateUserUseCase useCase = new AuthenticateUserUseCase(
                userRepository, authenticationService, tokenProvider
        );

        AuthenticateUserUseCase.AuthenticationResult result =
                useCase.execute(loginRequest.email(), loginRequest.password());

        return AuthResponseMapper.fromDomain(result.user(), result.token());
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        RegisterUserUseCase.RegistrationCommand command =
                new RegisterUserUseCase.RegistrationCommand(
                        registerRequest.email(),
                        registerRequest.password(),
                        registerRequest.passwordConfirmation()
                );

        RegisterUserUseCase useCase = new RegisterUserUseCase(
                userRepository,
                roleRepository,
                passwordEncoder,
                tokenProvider
        );

        RegisterUserUseCase.RegistrationResult result = useCase.execute(command);

        return AuthResponseMapper.fromDomain(result.user(), result.token());
    }

    @Transactional
    public void forgotPassword(String email) {
        new RequestPasswordResetUseCase(
                userRepository,
                passwordResetTokenRepository,
                emailService,
                emailTemplatePort,
                frontendUrl
        ).execute(email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        new ResetPasswordUseCase(
                passwordResetTokenRepository,
                userRepository,
                passwordEncoder
        ).execute(token, newPassword);
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        new ChangePasswordUseCase(userRepository, passwordEncoder)
                .execute(new ChangePasswordUseCase.Command(userId, currentPassword, newPassword));
    }
}
