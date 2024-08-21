package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Locality;
import com.example.adoptr_backend.model.Province;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.LocalityRepository;
import com.example.adoptr_backend.repository.ProvinceRepository;
import com.example.adoptr_backend.service.LocalityService;
import com.example.adoptr_backend.service.dto.request.LocalityDTOin;
import com.example.adoptr_backend.service.dto.response.LocalityDTO;
import com.example.adoptr_backend.service.mapper.LocalityMapper;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocalityServiceImpl implements LocalityService {

    private final LocalityRepository localityRepository;
    private final ProvinceRepository provinceRepository;

    public LocalityServiceImpl(LocalityRepository localityRepository, ProvinceRepository provinceRepository) {
        this.localityRepository = localityRepository;
        this.provinceRepository = provinceRepository;
    }

    @Override
    public LocalityDTO create(LocalityDTOin dto) {
        Locality locality = LocalityMapper.MAPPER.toEntity(dto);
        Optional<Province> provinceOptional = provinceRepository.findById(dto.getProvince_id());
        if (provinceOptional.isEmpty()) {
            throw new BadRequestException(Error.PROVINCE_NOT_FOUND);
        }
        Province province = provinceOptional.get();
        province.setId(dto.getProvince_id());
        locality.setProvince(province);
        locality = localityRepository.save(locality);
        return LocalityMapper.MAPPER.toDto(locality);
    }
}
