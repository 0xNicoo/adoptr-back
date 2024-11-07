package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void welcomeEmail(String to, String subject, String userName){
        try {
            Context context = new Context();

            //Variables del mail
            context.setVariable("name", userName);
            String contentHTML = templateEngine.process("welcomeEmail", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHTML, true);

            //Imagenes
            File imageFile = new File("src/main/resources/static/images/email-image.png");
            helper.addInline("welcome-image", imageFile);

            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Error al mandar mail: " + e.getMessage(), e);
        }
    }
}
