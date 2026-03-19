package com.accesosport.event.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistanceUnit {
    KM("km"),
    MI("mi");

    private final String symbol;
}
