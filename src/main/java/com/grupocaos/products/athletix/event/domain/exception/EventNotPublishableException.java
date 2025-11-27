package com.grupocaos.products.athletix.event.domain.exception;

public class EventNotPublishableException extends RuntimeException {

    /**
	 * Serial identifier
	 */
	private static final long serialVersionUID = 1540454714030567716L;

	public EventNotPublishableException(String message) {
        super(message);
    }
}
