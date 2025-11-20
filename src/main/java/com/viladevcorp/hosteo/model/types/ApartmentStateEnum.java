package com.viladevcorp.hosteo.model.types;

public enum ApartmentStateEnum {
    READY("READY"),
    OCCUPIED("OCCUPIED"),
    USED("USED");

    private final String value;
    ApartmentStateEnum(String value) {
        this.value = value;
    }
}
