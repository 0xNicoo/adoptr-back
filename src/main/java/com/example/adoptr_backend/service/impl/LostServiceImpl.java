package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.*;
import com.example.adoptr_backend.repository.LocalityRepository;
import com.example.adoptr_backend.repository.FoundRepository;
import com.example.adoptr_backend.repository.specification.LostSpec;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.LostService;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.LostDTOin;
import com.example.adoptr_backend.service.dto.request.LostFilterDTO;
import com.example.adoptr_backend.service.dto.request.LostStatusDTOin;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import com.example.adoptr_backend.service.mapper.LostMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.DistanceHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LostServiceImpl implements LostService {

    private final LostRepository lostRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    private final LocalityRepository localityRepository;

    private final FoundRepository foundRepository;

    private final ChatRepository chatRepository;


    public LostServiceImpl(LostRepository lostRepository,
                           UserRepository userRepository,
                           ImageService imageService,
                           LocalityRepository localityRepository,
                           ChatRepository chatRepository,
                           FoundRepository foundRepository)
    {
        this.lostRepository = lostRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.localityRepository = localityRepository;
        this.foundRepository = foundRepository;
        this.chatRepository = chatRepository;
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
        Locality locality = getLocality(dto);
        lost.setLocality(locality);
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
        List<LostDTO> sortedDtoList = page.stream().map(lost -> {
                    try {
                        LostDTO dto = LostMapper.MAPPER.toDto(lost);
                        String s3Url = imageService.getS3url(lost.getId(), ImageType.LOST);
                        dto.setS3Url(s3Url);
                        updateDistance(dto, filter.getLatitude(), filter.getLongitude());
                        return dto;
                    } catch (Exception e) {
                        System.err.println("Error al obtener la URL de la imagen para ID " + lost.getId() + ": " + e.getMessage());
                        LostDTO dto = LostMapper.MAPPER.toDto(lost);
                        dto.setS3Url(null);
                        return dto;
                    }
                })
                .sorted(Comparator.comparing(LostDTO::getDistanceWithoutUnit))
                .toList();

        return new PageImpl<>(sortedDtoList, pageable, page.getTotalElements());
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
        Locality locality = getLocality(dto);
        lost.setLocality(locality);
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

    @Override
    public void changeStatus(LostStatusDTOin dtoIn) {
        Long userId = AuthSupport.getUserId();
        Lost lost = getLost(dtoIn.getLostId());

        if(!userId.equals(lost.getUser().getId())){
            throw new BadRequestException(Error.USER_NOT_ADOPTION_OWNER);
        }

        if(dtoIn.getNextStatus() == LostStatusType.LOST && lost.getLostStatusType() == LostStatusType.FOUND) {
            foundRepository.deleteByLostId(lost.getId());
        }

        else if(dtoIn.getNextStatus() == LostStatusType.FOUND && dtoIn.getContactUserId() != null) {
            User contactUser = userRepository.findById(dtoIn.getContactUserId())
                    .orElseThrow(() -> new BadRequestException(Error.CONTACT_USER_NOT_FOUND));

            boolean chatExists = chatRepository.existsChatForPublicationBetweenUsers(
                    lost.getId(),
                    userId,
                    dtoIn.getContactUserId()
            );

            if (!chatExists) {
                throw new BadRequestException(Error.CHAT_NOT_FOUND);
            }

            Found found = new Found();
            found.setDate(LocalDateTime.now());
            found.setUser(contactUser);
            found.setLost(lost);
            foundRepository.save(found);
        }

        lost.setLostStatusType(dtoIn.getNextStatus());
        lostRepository.save(lost);
    }

    private Lost getLost(Long id) {
        Optional<Lost> lostOptional = lostRepository.findById(id);
        if(lostOptional.isEmpty()){
            throw new BadRequestException(Error.PUBLICATION_NOT_FOUND);
        }
        return lostOptional.get();
    }

    private Locality getLocality(LostDTOin dto) {
        Optional<Locality> localityOptional = localityRepository.findById(dto.getLocality_id());
        if (localityOptional.isEmpty()) {
            throw new BadRequestException(Error.LOCALITY_NOT_FOUND);
        }
        Locality locality = localityOptional.get();
        locality.setId(dto.getLocality_id());
        return locality;
    }

    private void updateDistance(LostDTO lost, double latitude, double longitude) {
        double distance = DistanceHelper.calculateDistance(lost, new Location(latitude, longitude));
        lost.setDistanceWithoutUnit(distance);
        DecimalFormat format = new DecimalFormat("#.##");
        String unit = (distance < 1) ? "Mts" : "Km";
        distance = (distance < 1) ? distance * 1000 : distance;
        lost.setDistance(format.format(distance) + " " + unit);
    }

}
