package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.FirebaseToken;
import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.repository.FirebaseTokenRepository;
import com.example.adoptr_backend.repository.PostRepository;
import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import com.example.adoptr_backend.service.mapper.FirebaseTokenMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseTokenRepository firebaseTokenRepository;

    private final PostRepository postRepository;

    public NotificationServiceImpl(FirebaseTokenRepository firebaseTokenRepository,
                                   PostRepository postRepository) {
        this.firebaseTokenRepository = firebaseTokenRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void sendLikeNotification(Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if(postOptional.isEmpty()){
            throw new BadRequestException(Error.POST_NOT_FOUND);
        }

        Optional<FirebaseToken> firebaseTokenOptional = firebaseTokenRepository.findByUserId(postOptional.get().getUser().getId());
        if(firebaseTokenOptional.isEmpty()){
            throw new BadRequestException(Error.USER_NOT_HAVE_FIREBASE_TOKEN);
        }

        Message message = Message.builder()
                .putData("title", "Like")
                .putData("body", "A alguien le gusto tu post")
                .setToken(firebaseTokenOptional.get().getToken())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
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

    private FirebaseToken saveFirebaseToken(String token, Long userId) {
        FirebaseToken firebaseToken = new FirebaseToken();
        firebaseToken.setToken(token);
        firebaseToken.setUpdatedAt(LocalDateTime.now());
        firebaseToken.setCreatedAt(LocalDateTime.now());
        firebaseToken.setUserId(userId);
        return firebaseTokenRepository.save(firebaseToken);
    }

}
