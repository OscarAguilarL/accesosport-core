package com.accesosport.user.domain.exception;

public class InvalidPasswordException extends RuntimeException {

	@java.io.Serial
	private static final long serialVersionUID = -688354940936810722L;

	public InvalidPasswordException(String messageKey) {
		super(messageKey);
	}
}
