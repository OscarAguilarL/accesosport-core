package com.accesosport.registration.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class RegistrationAccessDeniedException extends RuntimeException {

	private final Object[] args;

	public RegistrationAccessDeniedException(UUID registrationId, UUID requesterId) {
		super(MessageKeys.Registrations.REGISTRATION_ACCESS_DENIED);
		this.args = new Object[] { registrationId, requesterId };
	}
}
