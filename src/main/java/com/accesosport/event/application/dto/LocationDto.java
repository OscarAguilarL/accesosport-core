package com.accesosport.event.application.dto;

public record LocationDto(
        String place,
        String city,
        String country,
        Double latitude,
        Double longitude,
        String fullAddress
) {}
