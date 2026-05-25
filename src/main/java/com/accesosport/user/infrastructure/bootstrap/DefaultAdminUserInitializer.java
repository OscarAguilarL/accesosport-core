package com.accesosport.user.infrastructure.bootstrap;

import com.accesosport.auth.domain.service.PasswordEncoder;
import com.accesosport.bootstrap.domain.SystemInitializer;
import com.accesosport.shared.domain.valueobjects.Address;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.Role;
import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final OrganizerProfileRepository organizerProfileRepository;
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

        Role organizerRole = roleRepository.findByRole(RoleEnumeration.ROLE_ORGANIZER)
                .orElseThrow(() -> new IllegalStateException("ROLE_ORGANIZER not found"));

        PersonalData personalData = PersonalData.builder()
                .firstName("Admin")
                .lastName("Accesosport")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender("Other")
                .phoneNumber("0000000000")
                .build();

        Address address = new Address(
                "Calle Principal",
                "1",
                null,
                "Centro",
                "Ciudad de México",
                "CDMX",
                "México",
                "06000"
        );

        User adminUser = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole, organizerRole))
                .personalData(personalData)
                .address(address)
                .build();

        User savedUser = userRepository.save(adminUser);

        UserOrganizerProfile organizerProfile = UserOrganizerProfile.create(
                "Accesosport",
                null,
                null,
                null,
                "Default organizer profile for admin user",
                savedUser
        );
        organizerProfileRepository.save(organizerProfile);

        log.warn("Default admin user created: {} (CHANGE PASSWORD IN PRODUCTION!)", adminEmail);
    }

    @Override
    public boolean shouldExecute() {
        return userRepository.findByEmail(adminEmail).isEmpty();
    }
}
