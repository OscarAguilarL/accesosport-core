package com.grupocaos.products.athletix.shared.use_case.domain;

import lombok.extern.slf4j.Slf4j;

/**
 * AbstractUseCase is a base class that provides a template for implementing use cases.
 * It defines a standardized execution flow that includes validation, pre-execution logic,
 * main execution logic, and post-execution logic. Subclasses are responsible for
 * implementing the specific business logic in the {@code doExecute} method and may
 * optionally override other hook methods to customize the behavior.
 *
 * @param <C> the type of the input command used by the use case
 * @param <R> the type of the result produced by the use case
 */
@Slf4j
public abstract class AbstractUseCase<C, R> implements UseCase<C, R> {

    /**
     * Executes the use case by performing a predefined sequence of steps,
     * including validation, pre-execution logic, main execution, and post-execution logic.
     *
     * @param command the input command that drives the use case execution
     * @return the result produced by the successful execution of the use case
     * @throws NullPointerException if the provided command is null
     */
    @Override
    public final R execute(C command) {
        validate(command);

        beforeExecute(command);

        try {
            return doExecute(command);
        } finally {
            afterExecute(command);
        }
    }

    /**
     * Validates the input command before execution.
     * This method can be overridden by subclasses to implement specific validation logic.
     *
     * @param command the input command to be validated
     */
    protected void validate(C command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
    }

    /**
     * Executes the main logic of the use case. This method should be implemented
     * in subclasses to define the specific business logic for the use case.
     *
     * @param command the input command used to perform the use case logic
     * @return the result produced by the execution of the use case
     */
    protected abstract R doExecute(C command);

    /**
     * Hook method that is invoked before the main execution of the use case.
     * Can be overridden by subclasses to implement pre-execution logic,
     * such as initializing resources or preparing data.
     *
     * @param command the input command passed to the use case
     */
    protected void beforeExecute(C command) {
        log.debug("Executing use case: {} with command {}", getClass().getSimpleName(), command);
    }

    /**
     * Hook method that is invoked after the main execution of the use case.
     * Can be overridden by subclasses to perform post-execution logic,
     * such as cleanup tasks, logging, or monitoring.
     *
     * @param command the input command passed to the use case
     */
    protected void afterExecute(C command) {
        log.debug("Finished executing use case: {} with command {}", getClass().getSimpleName(), command);
    }

}
