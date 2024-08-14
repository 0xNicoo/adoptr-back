package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.mapper.ExampleMapper;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    @Override
    public ProfileDTO getById(Long id){
        Profile profile = getProfile(id);
        return ProfileMapper.MAPPER.toDto(profile);
    }

    private Profile getProfile(Long id) {
        Optional<Profile> profileOptional = profileRepository.findById(id);

        return profileOptional.get();
    }

    @Override
    public ProfileDTO update(Long id, ProfileDTOin dto){
        Profile profile = getProfile(id);
        Profile profileUpdated = ProfileMapper.MAPPER.toEntity(dto);
        ProfileMapper.MAPPER.update(profile, profileUpdated);
        profileRepository.save(profile);
        return ProfileMapper.MAPPER.toDto(profile);
    }

}
