package com.accesosport.event.domain.model;

import com.accesosport.shared.domain.i18n.MessageKeys;

public record Location(
		String place,
		String city,
		String country
		) {

    public static Location of(
    		String place,
    		String city,
    		String country
    		) {
        validate(place, city, country);
        return new Location(place, city, country);
    }

    private static void validate(String place, String city, String country) {
        if (place == null || place.isBlank()) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_PLACE_REQUIRED);
        }
    }

    public String getFullAddress() {
        StringBuilder result = new StringBuilder(place);
        if (city != null && !city.isBlank()) {
            result.append(", ").append(city);
        }
        if (country != null && !country.isBlank()) {
            result.append(", ").append(country);
        }
        return result.toString();
    }
}
