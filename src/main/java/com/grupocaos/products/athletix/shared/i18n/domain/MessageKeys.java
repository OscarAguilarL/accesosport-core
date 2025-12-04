package com.grupocaos.products.athletix.shared.i18n.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageKeys {

	public static class AuthMessages {
		private AuthMessages() {
		}

		public static final String AUTHENTICATION_FAILED = "auth.errors.auth-failed";
		public static final String EMAIL_ALREADY_EXISTS = "auth.errors.email-already-exists";
		public static final String PASSWORD_LENGTH_ERROR = "auth.errors.password-length-error";
		public static final String PASSWORDS_NOT_MATCH = "auth.errors.password-not-match";
		public static final String ROLE_NOT_FOUND = "auth.errors.role-not-found";
		public static final String EMAIl_REQUIRED = "auth.errors.email-required";
		public static final String PASSWORD_REQUIRED = "auth.errors.password-required";
		public static final String INVALID_EMAIL = "auth.errors.invalid-email";
		public static final String PASSWORD_CONFIRMATION_REQUIRED = "auth.errors.password-confirmation-required";
		public static final String ROLE_REQUIRED = "auth.errors.role-required";
		public static final String USER_NOT_FOUND = "auth.errors.user-not-found";
		public static final String INVALID_CREDENTIALS = "auth.errors.invalid-credentials";
	}
}
