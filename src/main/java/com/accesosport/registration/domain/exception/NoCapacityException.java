package com.accesosport.registration.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class NoCapacityException extends RuntimeException {

	private final Object[] args;

	public NoCapacityException(UUID eventId) {
		super(MessageKeys.Registrations.NO_CAPACITY);
		this.args = new Object[] { eventId };
	}
}
