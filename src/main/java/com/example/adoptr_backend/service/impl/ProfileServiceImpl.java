package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final ImageService imageService;

    public ProfileServiceImpl(UserRepository userRepository,
                              ProfileRepository profileRepository,
                              ImageService imageService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.imageService = imageService;
    }

    // TODO HACER EL BAD REQUEST PARA CUANDO EL USUARIO YA TIENE UN PERFIL
    @Override
    public ProfileDTO create(ProfileDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Optional<User> user = userRepository.findById(userId);
        Profile profile = ProfileMapper.MAPPER.toEntity(dto);
        profile.setUser(user.get());
        profile = profileRepository.save(profile);
        Long imageId = imageService.uploadImage(dto.getImage(), ImageType.PROFILE, profile.getId());
        profile.setImageId(imageId);
        profile = profileRepository.save(profile);
        return ProfileMapper.MAPPER.toDto(profile);
    }

    @Override
    public ProfileDTO getById(Long id){
        Profile profile = getProfile(id);
        ProfileDTO dto = ProfileMapper.MAPPER.toDto(profile);
        String s3Url = imageService.getS3url(id,ImageType.PROFILE);
        dto.setS3Url(s3Url);
        return dto;
    }


    @Override
    public ProfileDTO update(Long id, ProfileDTOin dto){
        Profile profile = getProfile(id);
        Profile profileUpdated = ProfileMapper.MAPPER.toEntity(dto);
        ProfileMapper.MAPPER.update(profile, profileUpdated);
        profileRepository.save(profile);
        return ProfileMapper.MAPPER.toDto(profile);
    }

    private Profile getProfile(Long id) {
        Optional<Profile> profileOptional = profileRepository.findById(id);

        return profileOptional.get();
    }

}
