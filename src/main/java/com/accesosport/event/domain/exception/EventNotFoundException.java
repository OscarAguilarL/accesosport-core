package com.accesosport.event.domain.exception;

import java.util.UUID;

import com.accesosport.shared.domain.i18n.MessageKeys;

import lombok.Getter;

@Getter
public class EventNotFoundException extends RuntimeException {

	private final Object[] args;

	public EventNotFoundException(UUID id) {
		super(MessageKeys.Events.EVENT_NOT_FOUND);
		this.args = new Object[] { id };
	}

	public EventNotFoundException(String message) {
		super(message);
		this.args = null;
	}
}
