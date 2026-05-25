package com.accesosport.image.presentation.exception;

import com.accesosport.image.domain.exception.EventImageNotFoundException;
import com.accesosport.image.domain.exception.ImageUploadException;
import com.accesosport.image.domain.exception.InvalidImageException;
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
@Slf4j
@RequiredArgsConstructor
public class ImageExceptionHandler {

    private final MessageTranslator messageTranslator;

    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidImage(InvalidImageException ex) {
        log.error("Invalid image: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Images.PROBLEM_INVALID_IMAGE));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/invalid-image"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ImageUploadException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ProblemDetail handleImageUploadFailed(ImageUploadException ex) {
        log.error("Image upload failed: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Images.PROBLEM_UPLOAD_FAILED));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/image-upload-failed"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(EventImageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleEventImageNotFound(EventImageNotFoundException ex) {
        log.error("Event image not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                messageTranslator.translate(ex.getMessage())
        );
        problemDetail.setTitle(messageTranslator.translate(MessageKeys.Images.PROBLEM_NOT_FOUND));
        problemDetail.setType(URI.create("https://api.accesosport.com/errors/image-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
