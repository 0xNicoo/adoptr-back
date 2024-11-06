package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.EmailDTOin;

public interface EmailService {
    void welcomeEmail(String to, String subject, String userName);
}
