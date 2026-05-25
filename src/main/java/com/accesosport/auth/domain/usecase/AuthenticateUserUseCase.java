package com.accesosport.auth.domain.usecase;

import com.accesosport.auth.domain.exception.InvalidCredentialsException;
import com.accesosport.auth.domain.service.AuthenticationService;
import com.accesosport.auth.domain.service.TokenProvider;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.auth.domain.exception.AuthenticationException;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
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
		} catch (UserNotFoundException | InvalidCredentialsException e) {
			throw e;
		} catch (Exception e) {
			throw new AuthenticationException(MessageKeys.AuthMessages.AUTHENTICATION_FAILED, e);
		}
	}

	public record AuthenticationResult(User user, String token) {
	}
}
