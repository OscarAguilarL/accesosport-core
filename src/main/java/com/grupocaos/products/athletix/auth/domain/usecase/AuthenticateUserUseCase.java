package com.grupocaos.products.athletix.auth.domain.usecase;

import com.grupocaos.products.athletix.auth.domain.service.AuthenticationService;
import com.grupocaos.products.athletix.auth.domain.service.TokenProvider;
import com.grupocaos.products.athletix.auth.domain.exception.AuthenticationException;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final TokenProvider tokenProvider;

    public AuthenticationResult execute(String email, String password) {
        try {
            User user = authenticationService.authenticate(email, password);
            user.updateLastAccess();
            userRepository.save(user);

            String token = tokenProvider.generateToken(user);
            return new AuthenticationResult(user, token);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed", e);
        }
    }

    public record AuthenticationResult(User user, String token) {
    }
}
