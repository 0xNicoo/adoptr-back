package com.adoptr.adoptr.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123456";
    private static final String OTHER_SECRET = "another-secret-another-secret-another-9999";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 3_600_000);

    @Test
    void tokenEmitidoDevuelveElIdInternoDelUsuario() {
        String token = jwtUtil.generateToken(42L);

        assertEquals(Optional.of(42L), jwtUtil.extractUserId(token));
    }

    @Test
    void elSubjectDelTokenEsElIdYNoElNombre() {
        String token = jwtUtil.generateToken(42L);

        String subject = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertEquals("42", subject);
    }

    @Test
    void tokenVencidoNoEsValido() {
        JwtUtil expired = new JwtUtil(SECRET, -1_000);

        assertTrue(jwtUtil.extractUserId(expired.generateToken(1L)).isEmpty());
    }

    @Test
    void tokenFirmadoConOtroSecretoNoEsValido() {
        JwtUtil other = new JwtUtil(OTHER_SECRET, 3_600_000);

        assertTrue(jwtUtil.extractUserId(other.generateToken(1L)).isEmpty());
    }

    @Test
    void tokenConSubjectNoNumericoNoEsValido() {
        String token = Jwts.builder()
                .setSubject("Dolores")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(jwtUtil.extractUserId(token).isEmpty());
    }

    @Test
    void textoQueNoEsUnJwtNoEsValido() {
        assertTrue(jwtUtil.extractUserId("esto-no-es-un-jwt").isEmpty());
        assertTrue(jwtUtil.extractUserId("").isEmpty());
    }
}
