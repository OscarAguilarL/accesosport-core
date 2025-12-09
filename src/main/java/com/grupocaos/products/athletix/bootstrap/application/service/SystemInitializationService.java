package com.grupocaos.products.athletix.bootstrap.application.service;

import com.grupocaos.products.athletix.bootstrap.domain.ExecuteSystemInitializersUseCase;
import com.grupocaos.products.athletix.bootstrap.domain.SystemInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to initialize data on startup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemInitializationService {

    private final List<SystemInitializer> systemInitializers;

    /**
     * Executes all system initializers
     */
    @Transactional
    public void initializeSystemData() {
        log.info("=== Starting system initialization ===");

        ExecuteSystemInitializersUseCase useCase = new ExecuteSystemInitializersUseCase(systemInitializers);

        ExecuteSystemInitializersUseCase.InitializaitonSummary summary = useCase.execute();

        if (summary.hasFailures()) {
            log.error("System initialization completed with failures: {}", summary);
        } else {
            log.info("=== System initialization completed successfully ===");
        }
    }
}
