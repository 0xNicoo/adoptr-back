package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.LostRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.repository.specification.LostSpec;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.LostService;
import com.example.adoptr_backend.service.dto.request.LostDTOin;
import com.example.adoptr_backend.service.dto.request.LostFilterDTO;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import com.example.adoptr_backend.service.mapper.LostMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class LostServiceImpl implements LostService {

    private final LostRepository lostRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;


    public LostServiceImpl(LostRepository lostRepository,
                               UserRepository userRepository,
                               ImageService imageService)
    {
        this.lostRepository = lostRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Override
    public LostDTO create(LostDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Optional<User> user = userRepository.findById(userId);
        Lost lost =  LostMapper.MAPPER.toEntity(dto);
        lost.setUser(user.get());
        lost.setType(PublicationType.LOST);
        lost = lostRepository.save(lost);
        Long imageId = imageService.uploadImage(dto.getImage(), ImageType.LOST, lost.getId());
        lost.setImageId(imageId);
        lost = lostRepository.save(lost);
        LostDTO lostDTO = LostMapper.MAPPER.toDto(lost);
        lostDTO.setS3Url(imageService.getS3url(lost.getId(), ImageType.LOST));
        return lostDTO;
    }

    @Override
    public LostDTO getById(Long id) {
        Lost lost = getLost(id);
        LostDTO dto =  LostMapper.MAPPER.toDto(lost);
        String s3Url = imageService.getS3url(id, ImageType.LOST);
        dto.setS3Url(s3Url);
        return dto;
    }

    @Override
    public Page<LostDTO> getAll(LostFilterDTO filter, Pageable pageable) {
        Specification<Lost> spec = LostSpec.getSpec(filter);
        Page<Lost> page = lostRepository.findAll(spec, pageable);
        Page<LostDTO> dtoPage = page.map(lost -> {
            try {
                LostDTO dto = LostMapper.MAPPER.toDto(lost);
                String s3Url = imageService.getS3url(lost.getId(), ImageType.LOST);
                dto.setS3Url(s3Url);
                return dto;
            } catch (Exception e) {
                System.err.println("Error al obtener la URL de la imagen para ID " + lost.getId() + ": " + e.getMessage());
                LostDTO dto = LostMapper.MAPPER.toDto(lost);
                dto.setS3Url(null);
                return dto;
            }
        });
        return dtoPage;
    }

    @Override
    public LostDTO update(Long id, LostDTOin dto) {
        Lost lost = getLost(id);
        Lost lostUpdated = LostMapper.MAPPER.toEntity(dto);
        LostMapper.MAPPER.update(lost, lostUpdated);
        if (dto.getImage() != null) {
            imageService.deleteImage(lost.getId(), ImageType.LOST);
            Long imageId = imageService.uploadImage(dto.getImage(), ImageType.LOST, lost.getId());
            lost.setImageId(imageId);
            lostRepository.save(lost);
        }
        lostRepository.save(lost);
        LostDTO lostDTO = LostMapper.MAPPER.toDto(lost);
        lostDTO.setS3Url(imageService.getS3url(lost.getId(), ImageType.LOST));
        return lostDTO;
    }

    @Override
    public void delete(Long id)  {
        Long userId = AuthSupport.getUserId();
        Lost lost = getLost(id);
        if(!Objects.equals(lost.getUser().getId(), userId)){
            throw new BadRequestException(Error.USER_NOT_LOST_OWNER);
        }
        lostRepository.delete(lost);
    }

    private Lost getLost(Long id) {
        Optional<Lost> lostOptional = lostRepository.findById(id);
        return lostOptional.get();
    }

}
