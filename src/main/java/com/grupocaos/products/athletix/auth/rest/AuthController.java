package com.grupocaos.products.athletix.auth.rest;

import com.grupocaos.products.athletix.auth.application.dto.AuthResponse;
import com.grupocaos.products.athletix.auth.application.dto.LoginRequest;
import com.grupocaos.products.athletix.auth.application.dto.RegisterRequest;
import com.grupocaos.products.athletix.auth.domain.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) throws BadRequestException {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) throws BadRequestException {
        return ResponseEntity.ok(authService.registerUser(registerRequest));
    }
}
