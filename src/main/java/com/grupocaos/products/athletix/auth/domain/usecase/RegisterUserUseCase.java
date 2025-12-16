package com.grupocaos.products.athletix.auth.domain.usecase;

import com.grupocaos.products.athletix.auth.domain.service.PasswordEncoder;
import com.grupocaos.products.athletix.auth.domain.service.TokenProvider;
import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;
import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import com.grupocaos.products.athletix.user.domain.exception.InvalidPasswordException;
import com.grupocaos.products.athletix.user.domain.exception.RoleNotFoundException;
import com.grupocaos.products.athletix.user.domain.exception.UserAlreadyExistsException;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;

import lombok.AllArgsConstructor;

/**
 * Handles the registration of a new user within the system. This use case
 * encompasses the validation of user input, creation of user entities,
 * assignment of default roles, encoding of passwords, saving user data to
 * the repository, and issuing a token for the registered user.
 */
@AllArgsConstructor
public class RegisterUserUseCase extends UseCase<RegisterUserUseCase.RegistrationCommand, RegisterUserUseCase.RegistrationResult> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public RegistrationResult internalExecute(RegistrationCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(MessageKeys.AuthMessages.EMAIL_ALREADY_EXISTS);
        }

        validatePassword(command.password(), command.passwordConfirmation());

        User user = User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .build();

        var userRole = roleRepository.findByRole(RoleEnumeration.ROLE_USER)
                .orElseThrow(() -> new RoleNotFoundException(MessageKeys.AuthMessages.ROLE_NOT_FOUND));
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser);

        return new RegistrationResult(savedUser, token);
    }

    private void validatePassword(String password, String passwordConfirmation) {
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException(MessageKeys.AuthMessages.PASSWORD_LENGTH_ERROR);
        }
        if (!password.equals(passwordConfirmation)) {
            throw new InvalidPasswordException(MessageKeys.AuthMessages.PASSWORDS_NOT_MATCH);
        }
    }

    /**
     * @param email                The email address of the user. Must be unique within the system and follow standard email formatting.
     * @param password             The password chosen by the user. Must meet security requirements such as minimum length.
     * @param passwordConfirmation The repeated password for confirmation. Must match the provided password exactly.
     */
    public record RegistrationCommand(String email, String password, String passwordConfirmation) {
    }

    /**
     * Represents the outcome of a user registration process.
     * This record encapsulates information about the newly registered user
     * and the authentication token generated for them.
     *
     * @param user  The {@link User} entity containing details about the registered user,
     *              including their email, password hash, roles, and timestamps.
     * @param token A string representing the authentication token issued for the user.
     *              This token is typically used for later authenticated operations.
     */
    public record RegistrationResult(User user, String token) {
    }
}
