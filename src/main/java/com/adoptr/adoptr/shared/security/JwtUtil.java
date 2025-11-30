package com.adoptr.adoptr.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public String generateToken(String username, Long id) {
        Map<String, Object> claims = Map.of(
                Claims.SUBJECT, username,
                Claims.ID, id,
                Claims.ISSUED_AT, new Date(),
                Claims.EXPIRATION, new Date(System.currentTimeMillis() + expiration)
        );
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)        // NO DEPRECADO
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
