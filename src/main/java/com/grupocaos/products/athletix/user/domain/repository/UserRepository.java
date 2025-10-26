package com.grupocaos.products.athletix.user.domain.repository;

import com.grupocaos.products.athletix.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(UUID id);
}
