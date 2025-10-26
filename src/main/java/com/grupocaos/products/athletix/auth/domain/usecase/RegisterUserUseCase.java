package com.grupocaos.products.athletix.auth.domain.usecase;

import com.grupocaos.products.athletix.auth.domain.service.PasswordEncoder;
import com.grupocaos.products.athletix.auth.domain.service.TokenProvider;
import com.grupocaos.products.athletix.user.domain.exception.RoleNotFoundException;
import com.grupocaos.products.athletix.user.domain.exception.UserAlreadyExistsException;
import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public RegistrationResult execute(RegistrationCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        validatePassword(command.password(), command.passwordConfirmation());

        User user = User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .build();

        Set<Role> roles = resolveRoles(command.roles());
        roles.forEach(user::addRole);

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser);

        return new RegistrationResult(savedUser, token);
    }

    private void validatePassword(String password, String passwordConfirmation) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!password.equals(passwordConfirmation)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }

    private Set<Role> resolveRoles(Set<String> roleStrings) {
        if (roleStrings == null || roleStrings.isEmpty()) {
            return Set.of(findRole(RoleEnumeration.ROLE_USER));
        }

        return roleStrings.stream()
                .map(this::mapToRoleEnumeration)
                .map(this::findRole)
                .collect(java.util.stream.Collectors.toSet());
    }

    private RoleEnumeration mapToRoleEnumeration(String roleStr) {
        return switch (roleStr.toLowerCase()) {
            case "admin" -> RoleEnumeration.ROLE_ADMIN;
            case "organizer" -> RoleEnumeration.ROLE_ORGANIZER;
            default -> RoleEnumeration.ROLE_USER;
        };
    }

    private Role findRole(RoleEnumeration roleEnumeration) {
        return roleRepository.findByRole(roleEnumeration)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
    }

    public record RegistrationCommand(String email, String password, String passwordConfirmation, Set<String> roles) {
    }

    public record RegistrationResult(User user, String token) {
    }
}
