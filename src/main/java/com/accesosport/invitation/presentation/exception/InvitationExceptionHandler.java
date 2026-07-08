package com.accesosport.invitation.presentation.exception;

import com.accesosport.invitation.domain.exception.InvitationAlreadyUsedException;
import com.accesosport.invitation.domain.exception.InvitationEmailMismatchException;
import com.accesosport.invitation.domain.exception.InvitationNotFoundException;
import com.accesosport.invitation.domain.exception.InvitationRevokedException;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.i18n.MessageTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class InvitationExceptionHandler {

    private final MessageTranslator messageTranslator;

    @ExceptionHandler(InvitationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(InvitationNotFoundException ex) {
        log.warn("Invitation not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                messageTranslator.translate(ex.getMessage())
        );
        pd.setTitle(messageTranslator.translate(MessageKeys.Invitations.PROBLEM_NOT_FOUND));
        pd.setType(URI.create("https://api.accesosport.com/errors/invitation-not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InvitationAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleAlreadyUsed(InvitationAlreadyUsedException ex) {
        log.warn("Invitation already used: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage())
        );
        pd.setTitle(messageTranslator.translate(MessageKeys.Invitations.PROBLEM_ALREADY_USED));
        pd.setType(URI.create("https://api.accesosport.com/errors/invitation-already-used"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InvitationRevokedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleRevoked(InvitationRevokedException ex) {
        log.warn("Invitation revoked: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                messageTranslator.translate(ex.getMessage())
        );
        pd.setTitle(messageTranslator.translate(MessageKeys.Invitations.PROBLEM_REVOKED));
        pd.setType(URI.create("https://api.accesosport.com/errors/invitation-revoked"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InvitationEmailMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleEmailMismatch(InvitationEmailMismatchException ex) {
        log.warn("Invitation email mismatch: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                messageTranslator.translate(ex.getMessage())
        );
        pd.setTitle(messageTranslator.translate(MessageKeys.Invitations.PROBLEM_EMAIL_MISMATCH));
        pd.setType(URI.create("https://api.accesosport.com/errors/invitation-email-mismatch"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
