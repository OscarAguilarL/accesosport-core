package com.accesosport.registration.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class RegistrationNotOpenException extends RuntimeException {

	private final Object[] args;

	public RegistrationNotOpenException(UUID eventId) {
		super(MessageKeys.Registrations.REGISTRATION_NOT_OPEN);
		this.args = new Object[] { eventId };
	}
}
