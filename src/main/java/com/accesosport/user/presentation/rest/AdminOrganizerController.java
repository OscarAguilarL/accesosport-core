package com.accesosport.user.presentation.rest;

import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.application.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/organizers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrganizerController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminOrganizerListItemResponse>> listOrganizers() {
        return ResponseEntity.ok(adminUserService.listOrganizers());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminOrganizerListItemResponse> approveOrganizer(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.approveOrganizer(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminOrganizerListItemResponse> rejectOrganizer(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.rejectOrganizer(id));
    }

    @PostMapping("/{id}/submit-review")
    public ResponseEntity<AdminOrganizerListItemResponse> submitOrganizerForReview(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.submitOrganizerForReview(id));
    }
}
