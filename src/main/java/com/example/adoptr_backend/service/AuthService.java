package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.UserDTOin;
import com.example.adoptr_backend.service.dto.response.UserDTO;

public interface AuthService {
    void register(UserDTOin dto);

    UserDTO authenticate(UserDTOin dto);
}
