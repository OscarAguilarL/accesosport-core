package com.accesosport.registration.domain.repository;

import com.accesosport.registration.domain.model.CheckinToken;

import java.util.Optional;

public interface CheckinTokenRepository {

    CheckinToken save(CheckinToken token);

    Optional<CheckinToken> findByToken(String token);
}
