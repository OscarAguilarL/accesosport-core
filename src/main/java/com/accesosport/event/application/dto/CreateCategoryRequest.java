package com.accesosport.event.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        UUID modalityId,
        @NotBlank @Size(max = 100) String name,
        Integer minAge,
        Integer maxAge
) {}
