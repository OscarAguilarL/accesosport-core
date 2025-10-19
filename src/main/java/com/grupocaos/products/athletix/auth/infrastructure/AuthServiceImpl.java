package com.grupocaos.products.athletix.auth.infrastructure;

import com.grupocaos.products.athletix.app.infrastructure.security.JwtTokenProvider;
import com.grupocaos.products.athletix.auth.application.dto.AuthResponse;
import com.grupocaos.products.athletix.auth.application.dto.LoginRequest;
import com.grupocaos.products.athletix.auth.application.dto.RegisterRequest;
import com.grupocaos.products.athletix.auth.domain.AuthService;
import com.grupocaos.products.athletix.user.domain.exception.RoleNotFoundException;
import com.grupocaos.products.athletix.user.domain.exception.UserAlreadyExistsException;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest) throws UserNotFoundException {
        try {
            String jwt = generateJwtToken(loginRequest.getEmail(), loginRequest.getPassword());

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setLastAccess(LocalDateTime.now());
            userRepository.save(user);

            return AuthResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .token(jwt)
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) throws UserAlreadyExistsException {
        validateRegistrationRequest(registerRequest);

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Error: Email already exists, please use another one");
        }

        User user = createUserFromRequest(registerRequest);
        Set<Role> roles = resolveUserRoles(registerRequest.getRole());

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        String jwt = generateJwtToken(registerRequest.getEmail(), registerRequest.getPassword());

        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles())
                .token(jwt)
                .build();
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalStateException("Password must be at least 8 characters");
        }

        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalStateException("Email must be an email address");
        }
    }

    private User createUserFromRequest(RegisterRequest registerRequest) {
        return User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .lastAccess(LocalDateTime.now())
                .build();
    }

    private Set<Role> resolveUserRoles(Set<String> strRoles) {
        if (strRoles == null || strRoles.isEmpty()) {
            return Set.of(findRoleByEnumeration(RoleEnumeration.ROLE_USER));
        }

        return strRoles.stream()
                .map(this::mapStringToRoleEnumeration)
                .map(this::findRoleByEnumeration)
                .collect(Collectors.toSet());
    }

    private RoleEnumeration mapStringToRoleEnumeration(String roleStr) {
        return switch (roleStr.toLowerCase()) {
            case "admin" -> RoleEnumeration.ROLE_ADMIN;
            case "organizer" -> RoleEnumeration.ROLE_ORGANIZER;
            default -> RoleEnumeration.ROLE_USER;
        };
    }

    private Role findRoleByEnumeration(RoleEnumeration roleEnum) {
        return roleRepository.findRoleByRole(roleEnum)
                .orElseThrow(() -> new RoleNotFoundException("Error: Role is not found."));
    }

    private String generateJwtToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }
}
