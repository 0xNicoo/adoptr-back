package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.LocalityDTOin;
import com.example.adoptr_backend.service.dto.response.LocalityDTO;

public interface LocalityService {
    LocalityDTO create(LocalityDTOin dto);

}
