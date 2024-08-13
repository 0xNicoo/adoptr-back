package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.UserDTOin;
import com.example.adoptr_backend.service.dto.response.UserDTO;

public interface AuthService {

    /**
     * Registra un usuario
     * @param dto
     */
    void register(UserDTOin dto);

    /**
     * Autentica al usuario
     * @param dto
     * @return
     */
    UserDTO authenticate(UserDTOin dto);
}
