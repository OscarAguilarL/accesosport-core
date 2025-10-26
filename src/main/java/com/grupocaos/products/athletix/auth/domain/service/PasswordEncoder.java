package com.grupocaos.products.athletix.auth.domain.service;

public interface PasswordEncoder {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
