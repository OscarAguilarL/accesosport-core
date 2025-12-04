package com.grupocaos.products.athletix.auth.domain.exception;

public class AuthenticationException extends RuntimeException {
	@java.io.Serial
	private static final long serialVersionUID = 406694814249606327L;

	public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
