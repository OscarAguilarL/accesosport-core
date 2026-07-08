package com.accesosport.auth.presentation.rest;

import com.accesosport.auth.application.dto.AuthResponse;
import com.accesosport.auth.application.dto.ChangePasswordRequest;
import com.accesosport.auth.application.dto.LoginRequest;
import com.accesosport.auth.application.dto.RegisterRequest;
import com.accesosport.auth.application.dto.RequestPasswordResetCommand;
import com.accesosport.auth.application.dto.ResetPasswordCommand;
import com.accesosport.auth.application.service.AuthApplicationService;
import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authApplicationService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authApplicationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/api/v1/public/auth/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody RequestPasswordResetCommand command) {
        authApplicationService.forgotPassword(command.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/public/auth/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        authApplicationService.resetPassword(command.token(), command.newPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/auth/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authApplicationService.changePassword(
                userDetails.getUserId(),
                request.currentPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok().build();
    }
}
