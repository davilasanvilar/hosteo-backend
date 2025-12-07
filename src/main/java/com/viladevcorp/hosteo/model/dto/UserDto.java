package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends BaseEntityDto {

  String email;

  String username;

  boolean validated;

  public UserDto(User user) {
    this.email = user.getEmail();
    this.username = user.getUsername();
    this.validated = user.isValidated();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    UserDto user = (UserDto) obj;
    return username.equals(user.username);
  }
}
