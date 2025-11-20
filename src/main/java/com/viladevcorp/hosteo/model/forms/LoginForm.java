package com.viladevcorp.hosteo.model.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LoginForm {

    private String username;
    private String password;
    private boolean rememberMe;

    public LoginForm(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

}
