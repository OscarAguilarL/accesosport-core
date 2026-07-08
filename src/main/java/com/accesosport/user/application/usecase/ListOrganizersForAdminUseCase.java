package com.accesosport.user.application.usecase;

import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListOrganizersForAdminUseCase
        extends UseCase<Void, List<AdminOrganizerListItemResponse>> {

    private final OrganizerProfileRepository organizerProfileRepository;

    @Override
    protected List<AdminOrganizerListItemResponse> internalExecute(Void command) {
        return organizerProfileRepository.findAll().stream()
                .map(AdminOrganizerListItemResponse::fromDomain)
                .toList();
    }
}
