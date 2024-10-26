package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.LocalityRepository;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ImageService imageService;
    private final LocalityRepository localityRepository;

    public ProfileServiceImpl(UserRepository userRepository,
                              ProfileRepository profileRepository,
                              ImageService imageService,
                              LocalityRepository localityRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.imageService = imageService;
        this.localityRepository = localityRepository;
    }


    @Override
    public ProfileDTO get() {
        Long userId = AuthSupport.getUserId();
        Profile profile = getProfileByUserId(userId);
        ProfileDTO dto = ProfileMapper.MAPPER.toDto(profile);
        String s3Url = imageService.getS3url(profile.getId(), ImageType.PROFILE);
        dto.setS3Url(s3Url);
        return dto;
    }

    @Transactional
    @Override
    public ProfileDTO create(ProfileDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Optional<User> user = userRepository.findById(userId);
        if (profileRepository.existsByUserId(userId)) {
            throw new BadRequestException(Error.PROFILE_ALREADY_EXIST);
        }
        Profile profile = ProfileMapper.MAPPER.toEntity(dto);
        profile.setUser(user.get());
        Locality locality = getLocality(dto);
        profile.setLocality(locality);
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
    public Long getProfileCount() {
        return profileRepository.count();
    }

    @Override
    public ProfileDTO update(Long id, ProfileDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Profile profile = getProfile(id);
        if (!profile.getUser().getId().equals(userId)) {
            throw new BadRequestException(Error.PROFILE_UPDATE_NOT_FOUND);
        }
        Profile profileUpdated = ProfileMapper.MAPPER.toEntity(dto);
        ProfileMapper.MAPPER.update(profile, profileUpdated);
        Locality locality = getLocality(dto);
        profile.setLocality(locality);
        profile = profileRepository.save(profile);

        if (dto.getImage() != null) {
            imageService.deleteImage(profile.getId(), ImageType.PROFILE);
            Long imageId = imageService.uploadImage(dto.getImage(), ImageType.PROFILE, profile.getId());
            profile.setImageId(imageId);
            profileRepository.save(profile);
        }

        return ProfileMapper.MAPPER.toDto(profile);
    }

    @Override
    public ProfileDTO getByUserId(Long userId) {
        Profile profile = getProfileByUserId(userId);
        ProfileDTO dto = ProfileMapper.MAPPER.toDto(profile);
        String s3Url = imageService.getS3url(profile.getId(), ImageType.PROFILE);
        dto.setS3Url(s3Url);
        return dto;
    }


    private Profile getProfile(Long id) {
        Optional<Profile> profileOptional = profileRepository.findById(id);
        if(profileOptional.isEmpty()){
            throw new BadRequestException(Error.PROFILE_NOT_FOUND);
        }
        return profileOptional.get();
    }

    private Profile getProfileByUserId(Long userId) {
        Optional<Profile> profileOptional = profileRepository.findByUserId(userId);
        if(profileOptional.isEmpty()){
            throw new BadRequestException(Error.PROFILE_NOT_FOUND);
        }
        return profileOptional.get();
    }

    private Locality getLocality(ProfileDTOin dto) {
        Optional<Locality> localityOptional = localityRepository.findById(dto.getLocality_id());
        if (localityOptional.isEmpty()) {
            throw new BadRequestException(Error.LOCALITY_NOT_FOUND);
        }
        Locality locality = localityOptional.get();
        locality.setId(dto.getLocality_id());
        return locality;
    }


}
