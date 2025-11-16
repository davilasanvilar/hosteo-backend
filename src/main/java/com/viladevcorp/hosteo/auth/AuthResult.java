package com.viladevcorp.hosteo.auth;

import java.util.UUID;

import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.dto.UserDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResult {

    private String authToken;
    private long authTokenExpiration;
    private String refreshToken;
    private UUID sessionId;
    private UserDto user;

    public AuthResult (User user) {
        this.user = new UserDto(user);
    }

    public AuthResult(String authToken, long authTokenExpiration, String refreshToken, UUID sessionId, UserDto user) {
        this.authToken = authToken;
        this.authTokenExpiration = authTokenExpiration;
        this.refreshToken = refreshToken;
        this.sessionId = sessionId;
        this.user = user;
    }
}
