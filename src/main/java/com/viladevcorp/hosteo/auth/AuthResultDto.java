package com.viladevcorp.hosteo.auth;

import java.util.UUID;

import com.viladevcorp.hosteo.model.dto.UserDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResultDto {

    private String authToken;
    private UUID sessionId;
    private UserDto user;

     public AuthResultDto(String authToken, UUID sessionId, UserDto user) {
        this.authToken = authToken;
        this.sessionId = sessionId;
        this.user = user;
    }
}
