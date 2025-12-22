package com.grupocaos.products.athletix.shared.domain.valueobjects;

import lombok.Getter;

public enum BloodType {

    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    @Getter
    private final String label;

    BloodType(String label) {
        this.label = label;
    }
}
