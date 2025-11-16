package com.template.backtemplate.auth;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResult {

    private String jwt;
    private Date expirationDate;

    public JwtResult(String jwt, Date expirationDate) {
        this.jwt = jwt;
        this.expirationDate = expirationDate;
    }
}
