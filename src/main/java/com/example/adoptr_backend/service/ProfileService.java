package com.example.adoptr_backend.service;


import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDTO get();

    ProfileDTO create(ProfileDTOin dto);

    ProfileDTO getById(Long id);

    ProfileDTO update(Long id, ProfileDTOin dto);

}
