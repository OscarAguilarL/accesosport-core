package com.grupocaos.products.athletix.bootstrap.infrastructure.config;

import com.grupocaos.products.athletix.bootstrap.application.service.SystemInitializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Bootstrap runner
 */
@Component
@RequiredArgsConstructor
public class BootstrapRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final SystemInitializationService initializationService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initializationService.initializeSystemData();
    }
}
