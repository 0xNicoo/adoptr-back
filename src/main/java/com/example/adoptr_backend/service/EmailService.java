package com.example.adoptr_backend.service;

public interface EmailService {
    void welcomeEmail(String to, String subject, String userName);
    void adoptionPublication(String to, String subject, String title, String id);
    void lostPublication(String to, String subject, String title, String id);
    void servicePublication(String to, String subject, String title, String id);
}
