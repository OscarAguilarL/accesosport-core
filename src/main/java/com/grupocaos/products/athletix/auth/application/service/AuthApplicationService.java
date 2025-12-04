package com.grupocaos.products.athletix.auth.application.service;

import com.grupocaos.products.athletix.auth.application.dto.AuthResponse;
import com.grupocaos.products.athletix.auth.application.dto.AuthResponseMapper;
import com.grupocaos.products.athletix.auth.application.dto.LoginRequest;
import com.grupocaos.products.athletix.auth.application.dto.RegisterRequest;
import com.grupocaos.products.athletix.auth.domain.service.AuthenticationService;
import com.grupocaos.products.athletix.auth.domain.service.PasswordEncoder;
import com.grupocaos.products.athletix.auth.domain.service.TokenProvider;
import com.grupocaos.products.athletix.auth.domain.usecase.AuthenticateUserUseCase;
import com.grupocaos.products.athletix.auth.domain.usecase.RegisterUserUseCase;
import com.grupocaos.products.athletix.shared.i18n.domain.MessageTranslator;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MessageTranslator messageTranslator;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        AuthenticateUserUseCase useCase = new AuthenticateUserUseCase(
                userRepository, authenticationService, tokenProvider, messageTranslator
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
                        registerRequest.passwordConfirmation(),
                        registerRequest.role()
                );

        RegisterUserUseCase useCase = new RegisterUserUseCase(
                userRepository,
                roleRepository,
                passwordEncoder,
                tokenProvider,
                messageTranslator
        );

        RegisterUserUseCase.RegistrationResult result = useCase.execute(command);

        return AuthResponseMapper.fromDomain(result.user(), result.token());
    }
}
