package com.example.adoptr_backend.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final long jwtExpiration;
    private final Algorithm signingAlgorithm;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long jwtExpiration
    ) {
        this.jwtExpiration = jwtExpiration;
        this.signingAlgorithm = Algorithm.HMAC256(secretKey);
    }

    public String extractEmail(String token) {
        JWTVerifier jwtVerifier = JWT.require(signingAlgorithm).build();
        DecodedJWT jwt = jwtVerifier.verify(token);
        return jwt.getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        try {
            JWTVerifier verifier = JWT.require(signingAlgorithm)
                    .withSubject(email)
                    .build();
            verifier.verify(token);
        } catch (JWTVerificationException exception) {
            return false;
        }
        return true;
    }

    public String buildToken(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(signingAlgorithm);
    }

}