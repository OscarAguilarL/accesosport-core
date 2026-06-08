package com.accesosport.invitation.domain.repository;

import com.accesosport.invitation.domain.model.InvitationToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationTokenRepository {
    Optional<InvitationToken> findByToken(UUID token);
    List<InvitationToken> findAll();
    InvitationToken save(InvitationToken token);
}
