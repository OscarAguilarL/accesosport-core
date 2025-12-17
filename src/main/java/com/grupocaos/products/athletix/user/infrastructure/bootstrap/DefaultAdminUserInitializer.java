package com.grupocaos.products.athletix.user.infrastructure.bootstrap;

import com.grupocaos.products.athletix.auth.domain.service.PasswordEncoder;
import com.grupocaos.products.athletix.bootstrap.domain.SystemInitializer;
import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Creates a default admin user for testing
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.bootstrap.default-admin",
        name = "enabled",
        havingValue = "true"
)
public class DefaultAdminUserInitializer implements SystemInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.default-admin.email}")
    private String adminEmail;

    @Value("${app.bootstrap.default-admin.password}")
    private String adminPassword;

    @Override
    public String getName() {
        return "Default admin user initializer";
    }

    @Override
    public Integer getOrder() {
        return 20;
    }

    @Override
    public void initialize() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Default admin user already exists");
            return;
        }

        Role adminRole = roleRepository.findByRole(RoleEnumeration.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

        User adminUser = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(adminUser);
        log.warn("Default admin user created: {} (CHANGE PASSWORD IN PRODUCTION!)", adminEmail);
    }

    @Override
    public boolean shouldExecute() {
        return userRepository.findByEmail(adminEmail).isEmpty();
    }
}
