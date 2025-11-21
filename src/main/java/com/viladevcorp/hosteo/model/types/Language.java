package com.viladevcorp.hosteo.model.types;

public enum Language {
    EN("EN"),
    ES("ES"),
    FR("FR"),
    DE("DE"),
    IT("IT"),
    PT("PT"),
    NL("NL"),
    PL("PL"),
    SV("SV"),
    NO("NO"),
    DA("DA"),
    FI("FI"),
    CS("CS"),
    HU("HU"),
    RO("RO"),
    BG("BG"),
    HR("HR"),
    SL("SL"),
    EL("EL"),
    TR("TR"),
    UK("UK"),
    RU("RU");

    private final String code;
    Language(String code) {
        this.code = code;
    }
}
