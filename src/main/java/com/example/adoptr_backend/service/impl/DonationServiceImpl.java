package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.Donation;
import com.example.adoptr_backend.repository.DonationRepository;
import com.example.adoptr_backend.repository.specification.DonationSpec;
import com.example.adoptr_backend.service.DonationService;
import com.example.adoptr_backend.service.dto.request.DonationDTOin;
import com.example.adoptr_backend.service.dto.request.DonationFilterDTO;
import com.example.adoptr_backend.service.dto.response.DonationDTO;
import com.example.adoptr_backend.service.mapper.DonationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;

    public DonationServiceImpl(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }
    @Override
    public DonationDTO create(DonationDTOin dto) {
        Donation donation = DonationMapper.MAPPER.toEntity(dto);
        donation = donationRepository.save(donation);
        return DonationMapper.MAPPER.toDto(donation);
    }

    @Override
    public Page<DonationDTO> getAll(DonationFilterDTO filter, Pageable pageable) {
        Specification<Donation> spec = DonationSpec.getSpec(filter);
        Page<Donation> page = donationRepository.findAll(spec, pageable);
        return page.map(DonationMapper.MAPPER::toDto);
    }
}
