package com.accesosport.registration.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class RegistrationNotFoundException extends RuntimeException {

	private final Object[] args;

	public RegistrationNotFoundException(UUID registrationId) {
		super(MessageKeys.Registrations.REGISTRATION_NOT_FOUND);
		this.args = new Object[] { registrationId };
	}

	public RegistrationNotFoundException(String ticketCode) {
		super(MessageKeys.Registrations.REGISTRATION_NOT_FOUND_BY_TICKET);
		this.args = new Object[] { ticketCode };
	}
}
