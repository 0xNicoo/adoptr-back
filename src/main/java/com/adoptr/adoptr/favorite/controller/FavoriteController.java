package com.adoptr.adoptr.favorite.controller;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.favorite.dto.response.FavoriteIdsDTO;
import com.adoptr.adoptr.favorite.service.FavoriteService;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.util.Paging;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/adoptions")
    public PageDTO<AdoptionSummaryDTO> adoptions(@RequestParam(required = false) Species species,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "" + Paging.DEFAULT_PAGE_SIZE) int pageSize) {
        return favoriteService.list(species, page, pageSize);
    }

    @GetMapping("/ids")
    public FavoriteIdsDTO ids() {
        return favoriteService.ids();
    }

    @PutMapping("/{publicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(@PathVariable Long publicationId) {
        favoriteService.save(publicationId);
    }

    @DeleteMapping("/{publicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsave(@PathVariable Long publicationId) {
        favoriteService.unsave(publicationId);
    }
}
