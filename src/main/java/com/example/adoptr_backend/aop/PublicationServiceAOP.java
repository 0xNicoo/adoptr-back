package com.example.adoptr_backend.aop;

import com.example.adoptr_backend.service.EmailService;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PublicationServiceAOP {

    private final EmailService emailService;

    public PublicationServiceAOP(EmailService emailService) {
        this.emailService = emailService;
    }

    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.AdoptionServiceImpl.create(..))", returning="dtoValue")
    public void notificationAdoptionPublication(JoinPoint joinPoint, Object dtoValue) {
        AdoptionDTO adoptionDTO = (AdoptionDTO) dtoValue;
        emailService.adoptionPublication(adoptionDTO.getUser().getEmail(), "Publicaste una mascota en adopción",adoptionDTO.getTitle(), adoptionDTO.getId().toString());
    }

    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.LostServiceImpl.create(..))", returning="dtoValue")
    public void notificationLostPublication(JoinPoint joinPoint, Object dtoValue) {
        LostDTO lostDTO = (LostDTO) dtoValue;
        emailService.lostPublication(lostDTO.getUser().getEmail(), "Publicaste una mascota perdida",lostDTO.getTitle(),lostDTO.getId().toString());
    }

    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.ServicesServiceImpl.create(..))", returning="dtoValue")
    public void notificationServicePublication(JoinPoint joinPoint, Object dtoValue) {
        ServiceDTO serviceDTO = (ServiceDTO) dtoValue;
        emailService.servicePublication(serviceDTO.getUser().getEmail(), "Publicaste un servicio",serviceDTO.getTitle(), serviceDTO.getId().toString());
    }

}
