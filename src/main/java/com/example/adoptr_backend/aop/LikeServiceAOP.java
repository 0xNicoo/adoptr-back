package com.example.adoptr_backend.aop;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Notification;
import com.example.adoptr_backend.model.NotificationModelType;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.response.LikesDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class LikeServiceAOP {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    public LikeServiceAOP(NotificationService notificationService,
                          UserRepository userRepository,
                          ProfileRepository profileRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Async
    @AfterReturning(pointcut="execution(* com.example.adoptr_backend.service.impl.LikesServiceImpl.set(..))", returning="dtoValue")
    public void notificationLike(JoinPoint joinPoint, Object dtoValue) {
        LikesDTO likesDTO = (LikesDTO) dtoValue;
        if(likesDTO != null){
            Notification notification = getNotification(likesDTO);
            notificationService.save(notification);
            notificationService.sendLikeNotification(likesDTO.getPost().getId(), notification.getUserName());
        }
    }

    private Notification getNotification(LikesDTO likesDTO) {
        Notification notification = new Notification();
        User userToNotify = getUser(likesDTO);
        if(likesDTO.getUserId().equals(userToNotify.getId())){
            throw new BadRequestException(Error.NOTIFICATION_SAME_USER);
        }
        Profile profile = getProfile(likesDTO);
        notification.setModelId(likesDTO.getId());
        notification.setModelType(NotificationModelType.LIKE);
        notification.setUser(userToNotify);
        notification.setUserName(profile.getFirstName() + " " + profile.getLastName());
        return notification;
    }

    private Profile getProfile(LikesDTO likesDTO) {
        Optional<Profile> profileOptional = profileRepository.findByUserId(likesDTO.getUserId());
        if(profileOptional.isEmpty()){
            throw new BadRequestException(Error.PROFILE_NOT_FOUND);
        }
        return profileOptional.get();
    }

    private User getUser(LikesDTO likesDTO) {
        Optional<User> userOptional = userRepository.findById(likesDTO.getPost().getUser().getId());
        if (userOptional.isEmpty()){
            throw new BadRequestException(Error.USER_NOT_FOUND);
        }
        return userOptional.get();
    }
}
