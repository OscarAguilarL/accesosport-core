package com.accesosport.event.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RaceType {
    MARATHON("Marathon", 42.195),
    HALF_MARATHON("Half Marathon", 21.0975),
    TEN_KM("10K", 10.0),
    FIVE_KM("5K", 5.0),
    OTHER("Other", null);

    private final String name;
    private final Double distance;
}
