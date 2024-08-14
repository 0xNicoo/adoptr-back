package com.example.adoptr_backend.service;


import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;

public interface ProfileService {
    ProfileDTO create(ProfileDTOin dto);

}
