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

    @Override
    public void adoptionPublication(String to, String subject, String title, String id){
        try {
            Context context = new Context();

            //Variables del mail
            context.setVariable("title", title);

            String publicationUrl = "http://localhost:3000/adopcion/" + id;
            context.setVariable("publicationUrl", publicationUrl);

            String contentHTML = templateEngine.process("adoptionPublication", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHTML, true);

            //Imagenes
            File imageFile = new File("src/main/resources/static/images/email-adoption.png");
            helper.addInline("adoption-image", imageFile);

            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Error al mandar mail: " + e.getMessage(), e);
        }
    }

    @Override
    public void lostPublication(String to, String subject, String title, String id){
        try {
            Context context = new Context();

            //Variables del mail
            context.setVariable("title", title);

            String publicationUrl = "http://localhost:3000/perdidas/" + id;
            context.setVariable("publicationUrl", publicationUrl);

            String contentHTML = templateEngine.process("lostPublication", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHTML, true);

            //Imagenes
            File imageFile = new File("src/main/resources/static/images/email-lost.png");
            helper.addInline("lost-image", imageFile);

            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Error al mandar mail: " + e.getMessage(), e);
        }
    }

    @Override
    public void servicePublication(String to, String subject, String title, String id){
        try {
            Context context = new Context();

            //Variables del mail
            context.setVariable("title", title);

            String publicationUrl = "http://localhost:3000/servicios/" + id;
            context.setVariable("publicationUrl", publicationUrl);

            String contentHTML = templateEngine.process("servicePublication", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHTML, true);

            //Imagenes
            File imageFile = new File("src/main/resources/static/images/email-service.png");
            helper.addInline("service-image", imageFile);

            javaMailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Error al mandar mail: " + e.getMessage(), e);
        }
    }
}
