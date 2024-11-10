package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;

public interface NotificationService {
    void sendLikeNotification(Long postId);

    FirebaseTokenDTO addUserToken(TokenDTO dto);
}
