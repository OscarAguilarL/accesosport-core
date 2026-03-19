package com.accesosport.user.domain.exception;

import lombok.Getter;

/**
 * Exception thrown to indicate an invalid state transition.
 * This is typically used in cases where an attempt is made to transition
 * an entity or object to a state that is not allowed.
 * <p>
 * The exception can include additional context such as the current state
 * and the attempted next state.
 */
public class InvalidVerificationStatusTransitionException extends RuntimeException {

    @Getter
    private final Object[] args;

    /**
     * Constructs an InvalidStatusTransitionException with a specific error message,
     * the current state, and the state to which a transition was attempted.
     *
     * @param message       the detail message explaining the reason for the exception
     * @param currentStatus the current state from which the transition was attempted
     * @param nextStatus    the state to which the transition was attempted
     */
    public InvalidVerificationStatusTransitionException(String message, String currentStatus, String nextStatus) {
        super(message);
        this.args = new Object[]{currentStatus, nextStatus};
    }

    /**
     * Constructs an InvalidStatusTransitionException with a specific error message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidVerificationStatusTransitionException(String message) {
        super(message);
        this.args = null;
    }
}
