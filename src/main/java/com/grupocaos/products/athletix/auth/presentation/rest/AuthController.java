package com.grupocaos.products.athletix.auth.presentation.rest;

import com.grupocaos.products.athletix.auth.application.dto.AuthResponse;
import com.grupocaos.products.athletix.auth.application.dto.LoginRequest;
import com.grupocaos.products.athletix.auth.application.dto.RegisterRequest;
import com.grupocaos.products.athletix.auth.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authApplicationService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authApplicationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }
}
