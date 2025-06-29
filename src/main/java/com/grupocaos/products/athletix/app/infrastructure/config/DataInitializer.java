package com.grupocaos.products.athletix.app.infrastructure.config;

import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        this.initializeRoles();
    }

    private void initializeRoles() {
        log.info("Initializing roles");

        for (RoleEnumeration roleEnum : RoleEnumeration.values()) {
            if (roleRepository.existsByRole(roleEnum)) {
                log.info("Role {} already exists", roleEnum);
            } else {
                Role role = Role.builder()
                        .role(roleEnum)
                        .build();
                roleRepository.save(role);
                log.info("Role {} created", roleEnum);
            }
        }
        log.info("Roles initialized");
    }
}
