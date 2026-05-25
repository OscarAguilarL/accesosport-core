package com.accesosport.auth.domain.service;

import com.accesosport.auth.domain.exception.InvalidCredentialsException;
import com.accesosport.user.domain.model.User;

public interface AuthenticationService {
    User authenticate(String email, String password) throws InvalidCredentialsException;
}
