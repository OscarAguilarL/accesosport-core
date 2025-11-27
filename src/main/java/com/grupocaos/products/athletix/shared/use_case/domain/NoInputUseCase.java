package com.grupocaos.products.athletix.shared.use_case.domain;

/**
 * NoInputUseCase is an abstract class that represents a use case without any input command.
 * It extends the functionality of the AbstractUseCase by ensuring that the input parameter
 * for execution and validation is always null. This class is designed for scenarios where
 * the business logic of a use case does not require any input data.
 *
 * @param <R> the type of the result produced by the use case
 */
public abstract class NoInputUseCase<R> extends AbstractUseCase<Void, R> {

    /**
     * Executes the use case with a standardized flow: pre-execution logic, main execution logic,
     * and post-execution logic. Since this use case does not require an input command, the methods
     * operate with a {@code null} parameter for consistency.
     *
     * @return the result produced by the implementation of the main business logic in the {@code doExecute} method
     */
    public final R execute() {
        beforeExecute(null);
        try {
            return doExecute(null);
        } finally {
            afterExecute(null);
        }
    }

    /**
     * Validates the input command for the use case. This implementation does not require
     * any specific validation as the use case operates without input.
     *
     * @param command the input command to be validated, which is always null for this use case
     */
    @Override
    protected void validate(Void command) {
        // No validation required for this use case
    }
}
