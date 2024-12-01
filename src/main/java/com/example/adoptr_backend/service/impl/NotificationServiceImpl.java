package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.FirebaseToken;
import com.example.adoptr_backend.model.Notification;
import com.example.adoptr_backend.model.NotificationModelType;
import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.repository.FirebaseTokenRepository;
import com.example.adoptr_backend.repository.NotificationRepository;
import com.example.adoptr_backend.repository.PostRepository;
import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import com.example.adoptr_backend.service.dto.response.NotificationDTO;
import com.example.adoptr_backend.service.mapper.FirebaseTokenMapper;
import com.example.adoptr_backend.service.mapper.NotificationMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.UrlSupport;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseTokenRepository firebaseTokenRepository;

    private final PostRepository postRepository;

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(FirebaseTokenRepository firebaseTokenRepository,
                                   PostRepository postRepository,
                                   NotificationRepository notificationRepository) {
        this.firebaseTokenRepository = firebaseTokenRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Override
    public void sendLikeNotification(Long postId, String userName) {
        Long userId = AuthSupport.getUserId();
        Optional<Post> postOptional = postRepository.findById(postId);
        if(postOptional.isEmpty()){
            throw new BadRequestException(Error.POST_NOT_FOUND);
        }
        Post post = postOptional.get();
        if(Objects.equals(post.getUser().getId(), userId)){
            throw new BadRequestException(Error.NOTIFICATION_SAME_USER);
        }
        Optional<FirebaseToken> firebaseTokenOptional = firebaseTokenRepository.findByUserId(post.getUser().getId());
        if(firebaseTokenOptional.isEmpty()){
            throw new BadRequestException(Error.USER_NOT_HAVE_FIREBASE_TOKEN);
        }

        Message message = Message.builder()
                .putData("modelId", String.valueOf(postId))
                .putData("modelType", NotificationModelType.LIKE.toString())
                .putData("url", UrlSupport.buildPostURL(postId))
                .putData("createdAt", LocalDateTime.now().toString())
                .putData("userName", userName)
                .setToken(firebaseTokenOptional.get().getToken())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Se rompio la notificacion :/");
        }
    }

    @Override
    public FirebaseTokenDTO addUserToken(TokenDTO dto) {
        Long userId = AuthSupport.getUserId();
        Optional<FirebaseToken> firebaseTokenOptional = firebaseTokenRepository.findByUserId(userId);
        if(firebaseTokenOptional.isPresent()){
            FirebaseToken firebaseToken = firebaseTokenOptional.get();
            if(!firebaseToken.getToken().equals((dto.getToken()))){
                firebaseToken.setToken(dto.getToken());
            }
            firebaseToken.setUpdatedAt(LocalDateTime.now());
            firebaseTokenRepository.save(firebaseToken);
            return FirebaseTokenMapper.MAPPER.toDto(firebaseToken);
        }
        FirebaseToken firebaseToken = saveFirebaseToken(dto.getToken(), userId);
        return FirebaseTokenMapper.MAPPER.toDto(firebaseToken);
    }

    @Override
    public void deleteNotification() {
        Long userId = AuthSupport.getUserId();
        notificationRepository.deleteByUserId(userId);
    }

    //TODO: Paginar esto
    @Override
    public List<NotificationDTO> getAll() {
        Long userId = AuthSupport.getUserId();
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByIdAsc(userId);
        List<NotificationDTO> notificationDTOList = NotificationMapper.MAPPER.toDto(notifications);
        notificationDTOList.forEach(n -> n.setUrl(getModelUrl(n)));
        return notificationDTOList;
    }

    private FirebaseToken saveFirebaseToken(String token, Long userId) {
        FirebaseToken firebaseToken = new FirebaseToken();
        firebaseToken.setToken(token);
        firebaseToken.setUpdatedAt(LocalDateTime.now());
        firebaseToken.setCreatedAt(LocalDateTime.now());
        firebaseToken.setUserId(userId);
        return firebaseTokenRepository.save(firebaseToken);
    }

    private String getModelUrl(NotificationDTO dto){
        return switch (dto.getModelType()){
            case LIKE -> UrlSupport.buildPostURL(dto.getModelId());
        };
    }

}
