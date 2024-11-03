package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.EmailDTOin;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(EmailDTOin emailDTOin);
}
