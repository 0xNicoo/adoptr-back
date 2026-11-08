package com.adoptr.adoptr.adoption.controller;

import com.adoptr.adoptr.adoption.dto.request.AdoptionDTOin;
import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.dto.request.AdoptionStatusDTOin;
import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.service.AdoptionService;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.util.Paging;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    /** Público: catálogo de publicaciones en adopción, de la más nueva a la más vieja. */
    @GetMapping("")
    public PageDTO<AdoptionSummaryDTO> search(@ModelAttribute AdoptionFilterDTO filter,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "" + Paging.DEFAULT_PAGE_SIZE) int pageSize) {
        return adoptionService.search(filter, page, pageSize);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public AdoptionDTO create(@Valid @RequestBody AdoptionDTOin request) {
        return adoptionService.create(request);
    }

    /** Público. La respuesta cambia según quién mira ({@code viewerIsOwner}): no cachear entre usuarios. */
    @GetMapping("/{id}")
    public ResponseEntity<AdoptionDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.empty().cachePrivate())
                .body(adoptionService.get(id));
    }

    @PutMapping("/{id}")
    public AdoptionDTO update(@PathVariable Long id, @Valid @RequestBody AdoptionDTOin request) {
        return adoptionService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public AdoptionDTO changeStatus(@PathVariable Long id, @Valid @RequestBody AdoptionStatusDTOin request) {
        return adoptionService.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adoptionService.delete(id);
    }

    @GetMapping("/mine")
    public PageDTO<AdoptionSummaryDTO> mine(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "" + Paging.DEFAULT_PAGE_SIZE) int pageSize) {
        return adoptionService.mine(page, pageSize);
    }
}
