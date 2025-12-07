package com.viladevcorp.hosteo.auth;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.exceptions.InvalidJwtException;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;

  @Autowired
  public JwtFilter(JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    log.info("Request: {} ", request.getRequestURI());
    ObjectMapper objectMapper = new ObjectMapper();
    if (!request.getRequestURI().matches("/api/public/.*")) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader == null) {
        ApiResponse<Void> apiResponse =
            new ApiResponse<>(CodeErrors.NOT_AUTH_JWT_TOKEN, "Not Authorization header present");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        return;
      }
      String[] authHeaderParts = authHeader.split(" ");
      if (authHeaderParts.length != 2) {
        ApiResponse<Void> apiResponse =
            new ApiResponse<>(CodeErrors.NOT_AUTH_JWT_TOKEN, "Not Authorization header present");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        return;
      }
      if (!authHeaderParts[0].equals("Bearer")) {
        ApiResponse<Void> apiResponse =
            new ApiResponse<>(CodeErrors.NOT_AUTH_JWT_TOKEN, "Not Authorization header present");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        return;
      }
      try {
        Authentication authToken = jwtUtils.validateToken(authHeaderParts[1]);
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (InvalidJwtException e) {
        ApiResponse<Void> apiResponse =
            new ApiResponse<>(CodeErrors.INVALID_TOKEN, "Invalid jwt token");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        return;
      }
      // Set the Authentication object to the SecurityContextHolder
      request.authenticate(response);
    }
    filterChain.doFilter(request, response);
  }
}
