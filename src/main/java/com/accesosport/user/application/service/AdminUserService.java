package com.accesosport.user.application.service;

import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.application.usecase.ApproveOrganizerVerificationUseCase;
import com.accesosport.user.application.usecase.ListOrganizersForAdminUseCase;
import com.accesosport.user.application.usecase.RejectOrganizerVerificationUseCase;
import com.accesosport.user.application.usecase.SubmitOrganizerForReviewUseCase;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final OrganizerProfileRepository organizerProfileRepository;

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
}
