package com.viladevcorp.hosteo.utils;

public enum ValidationCodeType {
    ACTIVATE_ACCOUNT("ACTIVATE_ACCOUNT"), RESET_PASSWORD("RESET_PASSWORD");

    private String type;

    ValidationCodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}