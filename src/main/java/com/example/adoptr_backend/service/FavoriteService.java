package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.response.FavoriteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface FavoriteService {

    /**
     * Crea/elimina un favorito de un usuario a una publicacion de adopcion
     * @param adoptionId
     * @return
     */
    FavoriteDTO set(Long adoptionId);

    /**
     * Obtiene el favorito por adoption y user
     * @return
     */
    FavoriteDTO get(Long adoptionId);

    /**
     * Obtiene los favoritos de un usuario
     * @return
     */
    Page<FavoriteDTO> getAll(Pageable pageable);

}
