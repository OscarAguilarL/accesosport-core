package com.accesosport.user.application.service;

import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.domain.port.EmailTemplatePort;
import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.application.usecase.ApproveOrganizerVerificationUseCase;
import com.accesosport.user.application.usecase.ListOrganizersForAdminUseCase;
import com.accesosport.user.application.usecase.RejectOrganizerVerificationUseCase;
import com.accesosport.user.application.usecase.SendStripeOnboardingReminderUseCase;
import com.accesosport.user.application.usecase.SubmitOrganizerForReviewUseCase;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final EmailService emailService;
    private final EmailTemplatePort emailTemplatePort;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public List<AdminOrganizerListItemResponse> listOrganizers() {
        return new ListOrganizersForAdminUseCase(organizerProfileRepository).execute();
    }

    @Transactional
    public AdminOrganizerListItemResponse approveOrganizer(UUID organizerProfileId) {
        return new ApproveOrganizerVerificationUseCase(organizerProfileRepository)
                .execute(new ApproveOrganizerVerificationUseCase.Command(organizerProfileId));
    }

    @Transactional
    public AdminOrganizerListItemResponse rejectOrganizer(UUID organizerProfileId) {
        return new RejectOrganizerVerificationUseCase(organizerProfileRepository)
                .execute(new RejectOrganizerVerificationUseCase.Command(organizerProfileId));
    }

    @Transactional
    public AdminOrganizerListItemResponse submitOrganizerForReview(UUID organizerProfileId) {
        return new SubmitOrganizerForReviewUseCase(organizerProfileRepository)
                .execute(new SubmitOrganizerForReviewUseCase.Command(organizerProfileId));
    }

    public void sendStripeOnboardingReminder(UUID organizerProfileId) {
        new SendStripeOnboardingReminderUseCase(organizerProfileRepository, emailService, emailTemplatePort, frontendUrl + "/dashboard")
                .execute(new SendStripeOnboardingReminderUseCase.Command(organizerProfileId));
    }
}
