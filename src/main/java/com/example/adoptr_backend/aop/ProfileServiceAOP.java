package com.example.adoptr_backend.aop;

import com.example.adoptr_backend.service.EmailService;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProfileServiceAOP {

    private final EmailService emailService;

    public ProfileServiceAOP(EmailService emailService) {
        this.emailService = emailService;
    }

    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.ProfileServiceImpl.create(..))", returning="dtoValue")
    public void notificationMailWelcome(JoinPoint joinPoint, Object dtoValue) {
        ProfileDTO profileDTO = (ProfileDTO) dtoValue;
        emailService.welcomeEmail(profileDTO.getUser().getEmail(), "Bienvenid@ a Adoptr", profileDTO.getFirstName());
    }

}
