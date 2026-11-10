package com.adoptr.adoptr.adoption.controller;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.service.AdoptionService;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.util.Paging;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publicaciones de adopción de un perfil, públicas. Vive en el módulo de adopción para que el de perfil
 * no dependa de él.
 */
@RestController
@RequestMapping("/profiles")
public class ProfileAdoptionsController {

    private final AdoptionService adoptionService;

    public ProfileAdoptionsController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    /** Sin {@code status}, en cualquier estado. */
    @GetMapping("/{profileId}/adoptions")
    public PageDTO<AdoptionSummaryDTO> list(@PathVariable Long profileId,
                                            @RequestParam(required = false) AdoptionStatus status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "" + Paging.DEFAULT_PAGE_SIZE) int pageSize) {
        return adoptionService.byProfile(profileId, status, page, pageSize);
    }
}
