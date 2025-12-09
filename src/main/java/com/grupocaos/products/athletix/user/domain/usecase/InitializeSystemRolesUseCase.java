package com.grupocaos.products.athletix.user.domain.usecase;

import com.grupocaos.products.athletix.shared.use_case.domain.UseCase;
import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Use case to initialize User Roles
 */
@Slf4j
@RequiredArgsConstructor
public class InitializeSystemRolesUseCase extends UseCase<Void, InitializeSystemRolesUseCase.InitializationResult> {

	private final RoleRepository roleRepository;

	@Override
	protected InitializationResult internalExecute(Void command) {

        RoleEnumeration[] rolesToInitialize = RoleEnumeration.values();
        int created = 0;
        int existing = 0;

        for (RoleEnumeration roleEnum: rolesToInitialize) {
            if (roleRepository.existsByRole(roleEnum)) {
                log.debug("Role {} already exists, skipping", roleEnum);
                existing++;
            } else {
                Role role = Role.builder()
                        .role(roleEnum)
                        .build();
                roleRepository.save(role);
                log.info("Role {} created succesfully", roleEnum);
                created++;
            }
        }

        log.info("System roles initialization " +
                "completed. Created {}, Existing: {}", created, existing);

        return new InitializationResult(created, existing);
	}

    /**
     * Result of the use case
     * @param rolesCreated Number of roles created after initialization
     * @param rolesExisting Number of roles existing before initialization
     */
    public record InitializationResult(Integer rolesCreated, Integer rolesExisting) {

        public int getTotal() {
            return rolesCreated + rolesExisting;
        }

        @Override
        public String toString() {
            return String.format("Roles initialized - Created: %d, Already existing: %d", rolesCreated, rolesExisting);
        }
    }

}
