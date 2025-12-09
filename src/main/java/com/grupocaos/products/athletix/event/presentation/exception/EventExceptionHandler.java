package com.grupocaos.products.athletix.event.presentation.exception;

import com.grupocaos.products.athletix.event.domain.exception.EventInvalidStatusException;
import com.grupocaos.products.athletix.event.domain.exception.EventNotFoundException;
import com.grupocaos.products.athletix.event.domain.exception.EventNotPublishableException;
import com.grupocaos.products.athletix.event.domain.exception.EventRegistrationClosedException;
import com.grupocaos.products.athletix.event.domain.exception.EventRegistrationFullException;
import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;
import com.grupocaos.products.athletix.shared.domain.i18n.MessageTranslator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler for managing exceptions related to event operations.
 * <p>
 * This class defines specific methods to handle various custom exceptions within the
 * application and returns a structured {@link ProblemDetail} response. It leverages
 * Spring Boot's {@code @RestControllerAdvice} annotation to intercept and process
 * exceptions thrown in the context of REST controllers.
 * </p>
 *
 * <p>
 * Additionally, this handler uses logging to record the occurrence of exceptions and
 * provides detailed information about each error type in the response payload.
 * </p>
 *
 * <h2>Exception Handling Includes:</h2>
 * <ul>
 *   <li>{@link EventNotFoundException}: Handles cases where the event is not found.</li>
 *   <li>{@link EventNotPublishableException}: Handles cases where an event cannot be published.</li>
 *   <li>{@link EventRegistrationFullException}: Handles cases where event registration is full.</li>
 *   <li>{@link EventRegistrationClosedException}: Handles cases where event registration is closed.</li>
 *   <li>{@link IllegalStateException}: Handles cases related to invalid state transitions.</li>
 *   <li>{@link IllegalArgumentException}: Handles cases of invalid arguments passed.</li>
 * </ul>
 *
 * <h2>Each Exception Handler:</h2>
 * <ul>
 *   <li>Returns an appropriate HTTP status code.</li>
 *   <li>Logs the exception with a descriptive error message.</li>
 *   <li>
 *     Constructs a {@link ProblemDetail} object containing:
 *     <ul>
 *       <li>A title describing the nature of the problem.</li>
 *       <li>A URI pointing to documentation or further information about the error.</li>
 *       <li>A timestamp indicating when the error occurred.</li>
 *     </ul>
 *   </li>
 * </ul>
 */

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class EventExceptionHandler {
	
	private final MessageTranslator messageTranslator;

    /**
     * Handles the {@link EventNotFoundException} when an event is not found in the system.
     * Converts the exception details into a {@link ProblemDetail} object with a 404 status.
     *
     * @param ex the {@link EventNotFoundException} that was thrown when the event could not be found
     * @return a {@link ProblemDetail} instance containing the error details, including status, title, type, and timestamp
     */
    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleEventNotFound(EventNotFoundException ex) {
        log.error("Evento not found: {}", ex.getMessage());
        
        String translatedMessage = messageTranslator.translate(ex.getMessage(), ex.getArgs());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                translatedMessage
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_NOT_FOUND));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/evento-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Handles the {@link EventNotPublishableException} when an event cannot be published.
     * Constructs a {@link ProblemDetail} object that includes HTTP status, error details, title, type, and timestamp.
     *
     * @param ex the {@link EventNotPublishableException} containing details about why the event is not publishable
     * @return a {@link ProblemDetail} instance representing the error, including a 400 BAD_REQUEST HTTP status
     */
    @ExceptionHandler(EventNotPublishableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleEventoNoPublishable(EventNotPublishableException ex) {
        log.error("Evento cannot be published: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_NOT_PUBLISHABLE));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/evento-not-publishable"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
    
    public ProblemDetail handleEventInvalidStatus(EventInvalidStatusException ex) {
    	log.error("Invalid event status: {}", ex.getArgs());
    	
    	ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
    			HttpStatus.BAD_REQUEST,
    			messageTranslator.translate(ex.getMessage(), ex.getArgs())
		);
    	problemDetail.setTitle(MessageKeys.Events.EVENT_PROBLEM_DETAIL_INVALID_STATUS);
    	problemDetail.setProperty("timestamp", Instant.now());
    	
    	return problemDetail;
    }

    /**
     * Handles the {@link EventRegistrationFullException} when an event has reached its registration capacity.
     * Converts the exception details into a {@link ProblemDetail} object with a 409 CONFLICT status.
     *
     * @param ex the {@link EventRegistrationFullException} that was thrown when the event is full
     * @return a {@link ProblemDetail} instance containing the error details, including status, title, type, and timestamp
     */
    @ExceptionHandler(EventRegistrationFullException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleEventRegistrationFull(EventRegistrationFullException ex) {
        log.error("Evento is full: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_REGISTRATION_FULL));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/evento-full"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Handles the {@link EventRegistrationClosedException} when event registration is closed.
     * Converts the exception details into a {@link ProblemDetail} object with a 409 CONFLICT status.
     *
     * @param ex the {@link EventRegistrationClosedException} that is thrown when registration is closed
     * @return a {@link ProblemDetail} instance containing the error details, including status, title, type, and timestamp
     */
    @ExceptionHandler(EventRegistrationClosedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleRegistrationClosed(EventRegistrationClosedException ex) {
        log.error("Registration closed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_REGISTRATION_CLOSED));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/registration-closed"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Handles the {@link IllegalStateException} when an illegal state transition occurs
     * within the application. Converts the exception details into a {@link ProblemDetail}
     * object with a 400 BAD_REQUEST status.
     *
     * @param ex the {@link IllegalStateException} that is thrown when an invalid
     *           state transition is attempted
     * @return a {@link ProblemDetail} instance representing the error, including
     * status, title, type, and a timestamp
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.error("Invalid state transition: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_OPERATION_NOT_ALLOWED));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/invalid-operation"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Handles the {@link IllegalArgumentException} thrown when an invalid argument is provided.
     * Converts the exception details into a {@link ProblemDetail} object with a 400 BAD_REQUEST status.
     *
     * @param ex the {@link IllegalArgumentException} that is thrown when an invalid argument is supplied
     * @return a {@link ProblemDetail} instance containing the error details, including status, title, type, and timestamp
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Events.EVENT_PROBLEM_DETAIL_VALIDATION_ERROR));
        problemDetail.setType(URI.create("https://api.athletix.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
