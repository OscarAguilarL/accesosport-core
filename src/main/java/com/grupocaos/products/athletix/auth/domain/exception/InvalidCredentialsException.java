package com.grupocaos.products.athletix.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {
	@java.io.Serial
	private static final long serialVersionUID = -2966777755586394477L;

	public InvalidCredentialsException(String message) {
		super(message);
	}
}
