package com.accesosport.registration.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class DuplicateRegistrationException extends RuntimeException {

	private final Object[] args;

	public DuplicateRegistrationException(UUID eventId, UUID participantId) {
		super(MessageKeys.Registrations.DUPLICATE_REGISTRATION);
		this.args = new Object[] { eventId, participantId };
	}
}
