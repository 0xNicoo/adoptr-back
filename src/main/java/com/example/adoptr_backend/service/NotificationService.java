package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Notification;
import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import com.example.adoptr_backend.service.dto.response.NotificationDTO;

import java.util.List;

public interface NotificationService {

    void save(Notification notification);

    void sendLikeNotification(Long postId, String userName);

    FirebaseTokenDTO addUserToken(TokenDTO dto);

    void deleteNotification();

    List<NotificationDTO> getAll();
}
