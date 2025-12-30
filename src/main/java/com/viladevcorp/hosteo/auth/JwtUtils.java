package com.viladevcorp.hosteo.auth;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.viladevcorp.hosteo.model.UserSession;
import com.viladevcorp.hosteo.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.viladevcorp.hosteo.exceptions.InvalidJwtException;
import com.viladevcorp.hosteo.service.CustomUserDetailsService;

import io.jsonwebtoken.*;

@Component
public class JwtUtils {
  @Value("${auth.jwt.secret}")
  private String jwtSecret;

  @Value("${auth.jwt.expiration}")
  private long expirationTime;

  private final CustomUserDetailsService customUserDetailsService;

  private final UserSessionRepository sessionRepository;

  @Autowired
  public JwtUtils(
      CustomUserDetailsService customUserDetailsService, UserSessionRepository sessionRepository) {
    this.customUserDetailsService = customUserDetailsService;
    this.sessionRepository = sessionRepository;
  }

  public JwtResult generateToken(
      UUID userId,
      String username,
      String email,
      long expirationTime,
      boolean rememberMe,
      UUID sessionId) {
    Date expirationDate = new Date(System.currentTimeMillis() + expirationTime * 1000);
    Claims claims = Jwts.claims();
    claims.put("email", email);
    claims.put("id", userId.toString());
    claims.put("username", username);
    claims.put("rememberMe", rememberMe);
    claims.put("sessionId", sessionId.toString());
    return new JwtResult(
        Jwts.builder()
            .setClaims(claims)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact(),
        expirationDate);
  }

  public boolean isExpired(String token) {
    return Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
  }

  public Claims extractClaims(String token) {
    return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
  }

  public Authentication validateToken(String token) throws InvalidJwtException {
    try {
      if (isExpired(token)) {
        throw new InvalidJwtException("Token expired");
      }
      Claims claims = extractClaims(token);
      String username = claims.get("username", String.class);
      if (username == null) {
        throw new InvalidJwtException("Invalid token");
      }
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      if (userDetails == null) {
        throw new InvalidJwtException("Invalid token");
      }
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, Set.<GrantedAuthority>of());
      String sessionId = claims.get("sessionId", String.class);
      UserSession userSession =
          sessionRepository.findByIdAndDeletedAtIsNull(UUID.fromString(sessionId)).orElse(null);
      if (userSession == null) {
        throw new InvalidJwtException("Invalid token");
      }
      authToken.setDetails(claims.get("sessionId", String.class));
      return authToken;
    } catch (Exception e) {
      throw new InvalidJwtException("Invalid token");
    }
  }
}
