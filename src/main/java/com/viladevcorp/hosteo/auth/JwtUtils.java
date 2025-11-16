package com.viladevcorp.hosteo.auth;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    @Autowired
    public JwtUtils(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    public JwtResult generateToken(UUID userId, String username, String email, long expirationTime, boolean rememberMe,
            UUID sessionId) {
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime * 1000);
        Claims claims = Jwts.claims();
        claims.put("email", email);
        claims.put("id", userId.toString());
        claims.put("username", username);
        claims.put("rememberMe", rememberMe);
        claims.put("sessionId", sessionId.toString());
        return new JwtResult(Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact(), expirationDate);
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
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
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
            return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null,
                    userDetails.getAuthorities());
        } catch (Exception e) {
            throw new InvalidJwtException("Invalid token");
        }
    }
}
