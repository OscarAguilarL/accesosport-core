package com.grupocaos.products.athletix.user.domain.exception;

import lombok.Getter;

/**
 * Exception thrown when a requested profile cannot be found.
 * This is typically used to signal that a profile-related operation
 * was attempted, but no matching profile could be located.
 * <p>
 * The exception includes a message that provides further details
 * about the cause or context of the error.
 */
public class ProfileNotFoundException extends RuntimeException {

    @Getter
    private final Object[] args;

    /**
     * Constructs a new ProfileNotFoundException with the specified detail message.
     * The message provides further details about the context or cause of the exception.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ProfileNotFoundException(String message) {
        super(message);
        this.args = null;
    }

    /**
     * Constructs a new ProfileNotFoundException with the specified detail message
     * and the type of profile that could not be found.
     *
     * @param message     the detail message explaining the reason for the exception
     * @param profileType the type of profile that was not found
     */
    public ProfileNotFoundException(String message, String profileType) {
        super(message);
        this.args = new Object[]{profileType};
    }
}
