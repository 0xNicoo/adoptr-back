package com.example.adoptr_backend.service;

public interface EmailService {
    void welcomeEmail(String to, String subject, String userName);
}
