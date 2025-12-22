package com.grupocaos.products.athletix.user.presentation.exception;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageTranslator;
import com.grupocaos.products.athletix.user.domain.exception.InvalidVerificationStatusTransitionException;
import com.grupocaos.products.athletix.user.domain.exception.ProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * A centralized exception handler for user-related operations within the application.
 * This class intercepts exceptions thrown by controller methods and provides custom handling logic.
 * It leverages the {@code MessageTranslator} for translating error messages based on
 * the current locale or specific locale as required.
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class UserExceptionHandler {

    private final MessageTranslator messageTranslator;

    /**
     * Handles exceptions of type {@link InvalidVerificationStatusTransitionException}.
     * Returns a {@link ProblemDetail} object containing details about the error.
     * The response status is set to {@code HttpStatus.BAD_REQUEST}.
     *
     * @param ex the exception containing information about the invalid status transition
     * @return a {@link ProblemDetail} object with the error details, including the translated
     * error message and a timestamp indicating when the error occurred
     */
    @ExceptionHandler(InvalidVerificationStatusTransitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidStatusTransition(InvalidVerificationStatusTransitionException ex) {
        log.error("Invalid status transition: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );

        problemDetail.setTitle(messageTranslator.translate(ex.getMessage(), ex.getArgs()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles the {@link ProfileNotFoundException} exception and returns a {@link ProblemDetail}
     * object containing details about the error. The response status is set to {@code HttpStatus.NOT_FOUND}.
     *
     * @param ex the exception representing that the requested profile could not be found
     * @return a {@link ProblemDetail} object with the error details, including the translated
     * error message and a timestamp indicating when the error occurred
     */
    @ExceptionHandler(ProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        log.error("Profile not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );

        problemDetail.setTitle(messageTranslator.translate(ex.getMessage()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
