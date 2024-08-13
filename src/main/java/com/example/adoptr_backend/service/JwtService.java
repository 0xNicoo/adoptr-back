package com.example.adoptr_backend.service;

public interface JwtService {
    String extractEmail(String token);

    boolean isTokenValid(String token, String email);

    String buildToken(String email);
}
