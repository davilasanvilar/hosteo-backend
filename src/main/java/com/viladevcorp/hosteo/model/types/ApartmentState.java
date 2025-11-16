package com.viladevcorp.hosteo.model.types;

public enum ApartmentState {
    READY("READY"),
    OCCUPIED("OCCUPIED"),
    USED("USED");

    private final String value;
    ApartmentState(String value) {
        this.value = value;
    }
}
