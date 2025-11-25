package com.grupocaos.products.athletix.auth.domain.service;

import com.grupocaos.products.athletix.auth.domain.exception.InvalidCredentialsException;
import com.grupocaos.products.athletix.user.domain.model.User;

public interface AuthenticationService {
    User authenticate(String email, String password) throws InvalidCredentialsException;
}
