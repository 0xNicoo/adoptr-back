package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.DonationDTOin;
import com.example.adoptr_backend.service.dto.request.DonationFilterDTO;
import com.example.adoptr_backend.service.dto.response.DonationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationService {
    DonationDTO create(DonationDTOin dto);

    Page<DonationDTO> getAll(DonationFilterDTO filterDTO, Pageable pageable);
}
