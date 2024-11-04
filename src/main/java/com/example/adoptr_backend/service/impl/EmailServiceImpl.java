package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.service.EmailService;
import com.example.adoptr_backend.service.dto.request.EmailDTOin;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendEmail(EmailDTOin dto){
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());

            Context context = new Context();
            context.setVariable("message", dto.getMessage());
            String contentHTML = templateEngine.process("email", context);
            helper.setText(contentHTML, true);
            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Error al mandar mail: " + e.getMessage(), e);
        }
    }
}
