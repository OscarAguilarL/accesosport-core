package com.grupocaos.products.athletix.auth.infrastructure.security.adapter;

import com.grupocaos.products.athletix.auth.domain.service.AuthenticationService;
import com.grupocaos.products.athletix.auth.domain.exception.InvalidCredentialsException;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAuthenticationServiceAdapter implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Override
    public User authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials");
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}
