package com.grupocaos.products.athletix.event.domain.exception;

import com.grupocaos.products.athletix.event.domain.model.EventStatus;

import lombok.Getter;

public class EventInvalidStatusException extends RuntimeException {

	@Getter
	private final Object[] args;

	public EventInvalidStatusException(String message, EventStatus status) {
		super(message);
		this.args = new Object[] { status };
	}

	public EventInvalidStatusException(String message) {
		super(message);
		this.args = null;
	}
}
