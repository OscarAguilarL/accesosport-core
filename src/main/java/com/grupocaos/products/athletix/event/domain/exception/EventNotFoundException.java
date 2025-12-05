package com.grupocaos.products.athletix.event.domain.exception;

import java.util.UUID;

import com.grupocaos.products.athletix.shared.i18n.domain.MessageKeys;

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
