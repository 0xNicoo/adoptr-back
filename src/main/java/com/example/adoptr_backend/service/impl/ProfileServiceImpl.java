package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.mapper.ExampleMapper;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileDTO create(ProfileDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Profile profile = ProfileMapper.MAPPER.toEntity(dto);
        profile.setCreatedByUser(userId);
        profile = profileRepository.save(profile);
        return ProfileMapper.MAPPER.toDto(profile);
    }
}
