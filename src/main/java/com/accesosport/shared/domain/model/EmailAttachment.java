package com.accesosport.shared.domain.model;

public record EmailAttachment(
        String filename,
        String contentType,
        byte[] content
) {}
