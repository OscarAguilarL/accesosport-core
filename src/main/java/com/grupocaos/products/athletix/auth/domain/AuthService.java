package com.grupocaos.products.athletix.auth.domain;

import com.grupocaos.products.athletix.auth.application.dto.AuthResponse;
import com.grupocaos.products.athletix.auth.application.dto.LoginRequest;
import com.grupocaos.products.athletix.auth.application.dto.RegisterRequest;
import org.apache.coyote.BadRequestException;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest registerRequest) throws BadRequestException;

    AuthResponse authenticateUser(LoginRequest loginRequest) throws BadRequestException;
}
