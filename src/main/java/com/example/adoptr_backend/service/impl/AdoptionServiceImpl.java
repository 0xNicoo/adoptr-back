package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.AdoptionRepository;
import com.example.adoptr_backend.repository.LocalityRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.repository.specification.AdoptionSpec;
import com.example.adoptr_backend.service.AdoptionService;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.AdoptionFilterDTO;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.mapper.AdoptionMapper;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionRepository adoptionRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    private final LocalityRepository localityRepository;


    public AdoptionServiceImpl(AdoptionRepository adoptionRepository,
                               UserRepository userRepository,
                               ImageService imageService,
                               LocalityRepository localityRepository)
                                {
        this.adoptionRepository = adoptionRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.localityRepository = localityRepository;
    }

    @Override
    public AdoptionDTO create(AdoptionDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Optional<User> user = userRepository.findById(userId);
        Adoption adoption =  AdoptionMapper.MAPPER.toEntity(dto);
        adoption.setUser(user.get());
        adoption.setType(PublicationType.ADOPTION);
        adoption = adoptionRepository.save(adoption);
        Long imageId = imageService.uploadImage(dto.getImage(), ImageType.ADOPTION, adoption.getId());
        adoption.setImageId(imageId);
        Locality locality = getLocality(dto);
        adoption.setLocality(locality);
        adoption = adoptionRepository.save(adoption);
        AdoptionDTO adoptionDTO = AdoptionMapper.MAPPER.toDto(adoption);
        adoptionDTO.setS3Url(imageService.getS3url(adoption.getId(), ImageType.ADOPTION));
        return adoptionDTO;
    }

    @Override
    public AdoptionDTO getById(Long id) {
        Adoption adoption = getAdoption(id);
        AdoptionDTO dto = AdoptionMapper.MAPPER.toDto(adoption);
        String s3Url = imageService.getS3url(id,ImageType.ADOPTION);
        dto.setS3Url(s3Url);
        return dto;
    }

    //TODO agregar que traiga tambien la imagen

    @Override
    public Page<AdoptionDTO> getAll(AdoptionFilterDTO filter, Pageable pageable) {
        Specification<Adoption> spec = AdoptionSpec.getSpec(filter);
        Page<Adoption> page = adoptionRepository.findAll(spec, pageable);
        return page.map(AdoptionMapper.MAPPER::toDto);
    }

    //TODO Agregar control de si suben una nueva imagen o no.
    //TODO Cuando se edita, en realidad se publica de nuevo la adopcion, hay que fixearlo
    @Override
    public AdoptionDTO update(Long id, AdoptionDTOin dto) {
        Adoption adoption = getAdoption(id);
        Adoption adoptionUpdated = AdoptionMapper.MAPPER.toEntity(dto);
        adoptionUpdated.setUser(adoption.getUser());
        adoptionUpdated.setType(PublicationType.ADOPTION);
        Long imageId = imageService.uploadImage(dto.getImage(), ImageType.ADOPTION, adoption.getId());
        adoptionUpdated.setImageId(imageId);
        Locality locality = getLocality(dto);
        adoptionUpdated.setLocality(locality);
        adoptionRepository.save(adoptionUpdated);
        AdoptionDTO adoptionDTO = AdoptionMapper.MAPPER.toDto(adoptionUpdated);
        adoptionDTO.setS3Url(imageService.getS3url(adoption.getId(), ImageType.ADOPTION));
        return adoptionDTO;
    }


    @Override
    public void delete(Long id)  {
        Adoption adoption = getAdoption(id);
        adoptionRepository.delete(adoption);
    }

    private Adoption getAdoption(Long id) {
        Optional<Adoption> adoptionOptional = adoptionRepository.findById(id);
        return adoptionOptional.get();
    }

    private Locality getLocality(AdoptionDTOin dto) {
        Optional<Locality> localityOptional = localityRepository.findById(dto.getLocality_id());
        if (localityOptional.isEmpty()) {
            throw new BadRequestException(Error.LOCALITY_NOT_FOUND);
        }
        Locality locality = localityOptional.get();
        locality.setId(dto.getLocality_id());
        return locality;
    }
}