package com.accesosport.auth.presentation.exception;

import com.accesosport.auth.domain.exception.AuthenticationException;
import com.accesosport.auth.domain.exception.InvalidCredentialsException;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.i18n.MessageTranslator;
import com.accesosport.user.domain.exception.InvalidPasswordException;
import com.accesosport.user.domain.exception.RoleNotFoundException;
import com.accesosport.user.domain.exception.UserAlreadyExistsException;
import com.accesosport.user.domain.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejo global de excepciones usando RFC 7807 Problem Details
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
	private final MessageTranslator messageTranslator;

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_USER_NOT_FOUND));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/user-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.error("User already exists: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_USER_ALREADY_EXISTS));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/user-exists"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleInvalidCredentials(Exception ex) {
        log.error("Invalid credentials: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_INVALID_CREDENTIALS));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/invalid-credentials"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_AUTH_ERROR));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/authentication-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleRoleNotFound(RoleNotFoundException ex) {
        log.error("Role not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_ROLE_NOT_FOUND));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/role-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_VALIDATION_ERROR));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

     @ExceptionHandler(InvalidPasswordException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex) {
    	log.error("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_VALIDATION_ERROR));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/invalid-password"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String translatedMessage = messageTranslator.translate(error.getDefaultMessage());
            errors.put(fieldName, translatedMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_VALIDATION_FAILED)
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.AuthMessages.PROBLEM_VALIDATION_ERROR));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }
}