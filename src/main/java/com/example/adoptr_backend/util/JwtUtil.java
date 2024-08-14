package com.example.adoptr_backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static long jwtExpiration;
    private static Algorithm signingAlgorithm;

    public JwtUtil(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long jwtExpiration
    ) {
        JwtUtil.jwtExpiration = jwtExpiration;
        signingAlgorithm = Algorithm.HMAC256(secretKey);
    }

    public static String extractEmail(String token) {
        JWTVerifier jwtVerifier = JWT.require(signingAlgorithm).build();
        DecodedJWT jwt = jwtVerifier.verify(token);
        return jwt.getSubject();
    }

    public static boolean isTokenValid(String token, String email) {
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

    public static String buildToken(String email, Long userId) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .withClaim("userId", userId)
                .sign(signingAlgorithm);
    }

}