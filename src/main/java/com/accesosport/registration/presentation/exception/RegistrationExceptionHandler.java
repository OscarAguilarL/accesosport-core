package com.accesosport.registration.presentation.exception;

import com.accesosport.registration.domain.exception.DuplicateRegistrationException;
import com.accesosport.registration.domain.exception.NoCapacityException;
import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.exception.RegistrationNotConfirmedException;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.exception.RegistrationNotOpenException;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.i18n.MessageTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RegistrationExceptionHandler {

    private final MessageTranslator messageTranslator;

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateRegistration(DuplicateRegistrationException ex) {
        log.error("Duplicate registration: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_DUPLICATE_REGISTRATION));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/duplicate-registration"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(RegistrationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(RegistrationNotFoundException ex) {
        log.error("Registration not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_REGISTRATION_NOT_FOUND));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/registration-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(RegistrationNotOpenException.class)
    public ResponseEntity<ProblemDetail> handleNotOpen(RegistrationNotOpenException ex) {
        log.error("Registration not open: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_REGISTRATION_NOT_OPEN));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/registration-not-open"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problemDetail);
    }

    @ExceptionHandler(NoCapacityException.class)
    public ResponseEntity<ProblemDetail> handleNoCapacity(NoCapacityException ex) {
        log.error("No capacity for registration: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_NO_CAPACITY));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/registration-no-capacity"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problemDetail);
    }

    @ExceptionHandler(RegistrationNotConfirmedException.class)
    public ResponseEntity<ProblemDetail> handleNotConfirmed(RegistrationNotConfirmedException ex) {
        log.error("Registration not confirmed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_REGISTRATION_NOT_CONFIRMED));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/registration-not-confirmed"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problemDetail);
    }

    @ExceptionHandler(RegistrationAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(RegistrationAccessDeniedException ex) {
        log.error("Registration access denied: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                messageTranslator.translate(ex.getMessage(), ex.getArgs())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Registrations.PROBLEM_REGISTRATION_ACCESS_DENIED));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/registration-access-denied"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }
}
