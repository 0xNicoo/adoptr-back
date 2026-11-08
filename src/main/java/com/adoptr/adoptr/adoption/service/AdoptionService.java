package com.adoptr.adoptr.adoption.service;

import com.adoptr.adoptr.adoption.dto.request.AdoptionDTOin;
import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.dto.request.AdoptionStatusDTOin;
import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.shared.dto.PageDTO;

public interface AdoptionService {

    AdoptionDTO create(AdoptionDTOin request);

    AdoptionDTO get(Long id);

    AdoptionDTO update(Long id, AdoptionDTOin request);

    AdoptionDTO changeStatus(Long id, AdoptionStatusDTOin request);

    void delete(Long id);

    PageDTO<AdoptionSummaryDTO> mine(int page, int pageSize);

    /** Catálogo público: solo publicaciones en adopción, con los filtros que vengan. */
    PageDTO<AdoptionSummaryDTO> search(AdoptionFilterDTO filter, int page, int pageSize);
}
