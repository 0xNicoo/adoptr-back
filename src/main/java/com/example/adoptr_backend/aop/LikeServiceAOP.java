package com.example.adoptr_backend.aop;

import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.response.LikesDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LikeServiceAOP {

    private final NotificationService notificationService;

    public LikeServiceAOP(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.LikesServiceImpl.set(..))", returning="dtoValue")
    public void notificationLike(JoinPoint joinPoint, Object dtoValue) {
        LikesDTO likesDTO = (LikesDTO) dtoValue;
        if(likesDTO != null){
            notificationService.sendLikeNotification(likesDTO.getPostId());
        }
    }
}
