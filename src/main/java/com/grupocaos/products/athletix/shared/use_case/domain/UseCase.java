package com.grupocaos.products.athletix.shared.use_case.domain;

/**
 * A generic interface that represents a use case in an application.
 * Use cases define the application-specific business logic and act as the
 * central point for orchestrating interactions between different layers
 * (e.g., services, repositories, or external systems).
 *
 * @param <Command> the type of input data required to execute the use case
 * @param <Result> the type of output data produced by the use case
 */
public interface UseCase<Command, Result> {
    /**
     * Executes the use case with the provided input command and returns the corresponding result.
     * The exact behavior of the execution is defined by the implementing class.
     *
     * @param command the input command containing the data necessary to execute the use case
     * @return the result produced by the execution of the use case
     */
    Result execute(Command command);
}
