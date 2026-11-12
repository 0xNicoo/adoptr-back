package com.adoptr.adoptr.favorite.service;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.favorite.dto.response.FavoriteIdsDTO;
import com.adoptr.adoptr.shared.dto.PageDTO;

public interface FavoriteService {

    void save(Long publicationId);

    void unsave(Long publicationId);

    PageDTO<AdoptionSummaryDTO> list(Species species, int page, int pageSize);

    FavoriteIdsDTO ids();
}
