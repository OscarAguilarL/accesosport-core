package com.accesosport.event.infrastructure.scheduler;

import com.accesosport.event.application.service.EmailReminderService;
import com.accesosport.event.application.service.EventLifecycleService;
import com.accesosport.registration.application.service.RegistrationCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventLifecycleScheduler {

    private final EventLifecycleService lifecycleService;
    private final RegistrationCleanupService registrationCleanupService;
    private final EmailReminderService emailReminderService;

    @Scheduled(fixedDelayString = "${app.scheduler.event-lifecycle.fixed-delay-ms:60000}")
    public void runEventLifecycleTransitions() {
        log.debug("[Scheduler] Running event lifecycle transitions");
        lifecycleService.autoOpenRegistrations();
        lifecycleService.autoCloseRegistrations();
        lifecycleService.autoBeginEvents();
        lifecycleService.autoCompleteEvents();
        registrationCleanupService.cleanupExpiredPendingPayments();
    }

    @Scheduled(fixedDelayString = "${app.scheduler.reminder.fixed-delay-ms:3600000}")
    public void runEventReminders() {
        log.debug("[Scheduler] Running event reminder check");
        try {
            emailReminderService.sendEventReminders();
        } catch (Exception e) {
            log.error("[Scheduler] Event reminder check failed", e);
        }
    }
}
