package com.adoptr.adoptr.adoption.service;

import com.adoptr.adoptr.adoption.dto.request.AdoptionDTOin;
import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.dto.request.AdoptionStatusDTOin;
import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.shared.dto.PageDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AdoptionService {

    AdoptionDTO create(AdoptionDTOin request);

    AdoptionDTO get(Long id);

    AdoptionDTO update(Long id, AdoptionDTOin request);

    AdoptionDTO changeStatus(Long id, AdoptionStatusDTOin request);

    void delete(Long id);

    /** Agrega una foto al final; la primera que queda es la portada. */
    AdoptionDTO addPhoto(Long id, MultipartFile file);

    void deletePhoto(Long id, Long photoId);

    /** Publicaciones activas del usuario del token; {@code status} null trae todos los estados. */
    PageDTO<AdoptionSummaryDTO> mine(AdoptionStatus status, int page, int pageSize);

    /**
     * Publicaciones activas del perfil indicado, públicas; {@code status} null trae todos los estados.
     * 404 {@code PROFILE_NOT_FOUND} si el perfil no existe.
     */
    PageDTO<AdoptionSummaryDTO> byProfile(Long profileId, AdoptionStatus status, int page, int pageSize);

    /** Catálogo público: solo publicaciones en adopción, con los filtros que vengan. */
    PageDTO<AdoptionSummaryDTO> search(AdoptionFilterDTO filter, int page, int pageSize);
}
