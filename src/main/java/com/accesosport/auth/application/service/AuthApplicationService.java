package com.accesosport.auth.application.service;

import com.accesosport.auth.application.dto.AuthResponse;
import com.accesosport.auth.application.dto.AuthResponseMapper;
import com.accesosport.auth.application.dto.LoginRequest;
import com.accesosport.auth.application.dto.RegisterRequest;
import com.accesosport.auth.domain.service.AuthenticationService;
import com.accesosport.auth.domain.service.PasswordEncoder;
import com.accesosport.auth.domain.service.TokenProvider;
import com.accesosport.auth.domain.usecase.AuthenticateUserUseCase;
import com.accesosport.auth.domain.usecase.RegisterUserUseCase;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.domain.repository.UserRepository;
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
}
