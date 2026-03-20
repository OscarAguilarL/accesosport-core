package com.accesosport.image.domain.exception;

public class ImageUploadException extends RuntimeException {

    public ImageUploadException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }

    public ImageUploadException(String messageKey) {
        super(messageKey);
    }
}
