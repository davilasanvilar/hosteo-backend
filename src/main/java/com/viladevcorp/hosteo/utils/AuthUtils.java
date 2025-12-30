package com.viladevcorp.hosteo.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.BaseEntity;
import com.viladevcorp.hosteo.model.User;

import java.util.UUID;

public class AuthUtils {

  public static void checkIfCreator(BaseEntity entity, String resourceName)
      throws NotAllowedResourceException {
    boolean isCreator = checkIfLoggedUser(entity.getCreatedBy());
    if (!isCreator) {
      throw new NotAllowedResourceException(
          "You are not allowed to access this "
              + (resourceName == null ? "resource" : resourceName.toLowerCase())
              + ".");
    }
  }

  public static boolean checkIfLoggedUser(User user) {
    return user.getUsername().equals(getUsername());
  }

  public static String getUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuth(auth)) {
      return null;
    }
    return auth.getName();
  }

  public static User getAuthUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuth(auth)) {
      return null;
    }
    return (User) auth.getPrincipal();
  }

  public static boolean isAuth(Authentication auth) {
    return auth != null
        && auth.isAuthenticated()
        && !(auth instanceof AnonymousAuthenticationToken);
  }

  public static UUID getSessionId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuth(auth)) {
      return null;
    }
    return auth.getDetails() == null ? null : UUID.fromString(auth.getDetails().toString());
  }
}
