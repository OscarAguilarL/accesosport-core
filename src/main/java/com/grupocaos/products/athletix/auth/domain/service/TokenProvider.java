package com.grupocaos.products.athletix.auth.domain.service;

import com.grupocaos.products.athletix.user.domain.model.User;

public interface TokenProvider {
    String generateToken(User user);

    String extractUsername(String token);

    boolean validateToken(String token);
}
