package com.accesosport.registration.domain.exception;

import java.util.UUID;

public class NoCapacityException extends RuntimeException {

	public NoCapacityException(UUID eventId) {
		super("No available capacity for event: " + eventId);
	}
}
