package com.accesosport.auth.infrastructure.security.adapter;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.accesosport.auth.domain.exception.InvalidCredentialsException;
import com.accesosport.auth.domain.service.AuthenticationService;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAuthenticationServiceAdapter implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public User authenticate(String email, String password) throws InvalidCredentialsException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials");
            throw new InvalidCredentialsException(MessageKeys.AuthMessages.INVALID_CREDENTIALS);
        }
    }
}
