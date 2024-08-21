package com.example.adoptr_backend.service.impl;


import com.example.adoptr_backend.repository.ProvinceRepository;
import com.example.adoptr_backend.model.Province;
import com.example.adoptr_backend.repository.specification.ProvinceSpec;
import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import com.example.adoptr_backend.service.mapper.ProvinceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;

    public ProvinceServiceImpl(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }
    @Override
    public ProvinceDTO create(ProvinceDTOin dto) {
        Province province = ProvinceMapper.MAPPER.toEntity(dto);
        province = provinceRepository.save(province);
        return ProvinceMapper.MAPPER.toDto(province);
    }

    @Override
    public Page<ProvinceDTO> getAll(ProvinceFilterDTO filter, Pageable pageable) {
        Specification<Province> spec = ProvinceSpec.getSpec(filter);
        Page<Province> page = provinceRepository.findAll(spec, pageable);
        return page.map(ProvinceMapper.MAPPER::toDto);
    }
}
