package com.accesosport.user.infrastructure.bootstrap;

import com.accesosport.user.domain.usecase.InitializeSystemRolesUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.accesosport.bootstrap.domain.SystemInitializer;
import com.accesosport.user.domain.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Roles system initializer
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		prefix = "app.bootstrap.roles",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class RolesSystemInitializer implements SystemInitializer {

	private final RoleRepository roleRepository;

	@Override
	public String getName() {
		return "System roles initializer";
	}

	@Override
	public Integer getOrder() {
		return 10;
	}

	@Override
	public void initialize() {
        InitializeSystemRolesUseCase useCase = new InitializeSystemRolesUseCase(roleRepository);
        useCase.execute();
	}
}
