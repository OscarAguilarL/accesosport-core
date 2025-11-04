package com.grupocaos.products.athletix.event.domain.model;


public record Location(String place, String city, String country, Double latitude, Double longitude) {

    public static Location of(String place, String city, String country, Double latitude, Double longitude) {
        validate(place, city, country, latitude, longitude);
        return new Location(place, city, country, latitude, longitude);
    }

    private static void validate(String place, String city, String country, Double latitude, Double longitude) {
        if (place == null || place.isBlank()) {
            throw new IllegalArgumentException("Place is required");
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Invalid latitude " + latitude);
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Invalid longitude " + longitude);
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
