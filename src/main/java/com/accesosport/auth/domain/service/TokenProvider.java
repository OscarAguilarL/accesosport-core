package com.accesosport.auth.domain.service;

import com.accesosport.user.domain.model.User;

public interface TokenProvider {
    String generateToken(User user);

    String extractUsername(String token);

    boolean validateToken(String token);
}
