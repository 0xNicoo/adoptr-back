package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.model.Favorite;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.AdoptionRepository;
import com.example.adoptr_backend.repository.FavoriteRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.FavoriteService;
import com.example.adoptr_backend.service.dto.response.FavoriteDTO;
import com.example.adoptr_backend.service.mapper.FavoriteMapper;
import com.example.adoptr_backend.util.AuthSupport;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final UserRepository userRepository;
    private final AdoptionRepository adoptionRepository;

    @Override
    public FavoriteDTO set(Long adoptionId) {
        Long userId = AuthSupport.getUserId();
        Optional<Favorite> favoriteOptional = favoriteRepository.findByAdoptionIdAndUserId(adoptionId, userId);
        if(favoriteOptional.isEmpty()){
            Favorite favorite = new Favorite();
            Adoption adoption = getAdoption(adoptionId);
            favorite.setUserId(userId);
            favorite.setAdoptionId(adoption.getId());
            favorite = favoriteRepository.save(favorite);
            return FavoriteMapper.MAPPER.toDto(favorite);
        }
        favoriteRepository.delete(favoriteOptional.get());
        return null;
    }

    @Override
    public FavoriteDTO get(Long adoptionId) {
        Long userId = AuthSupport.getUserId();
        Optional<Favorite> favoriteOptional = favoriteRepository.findByAdoptionIdAndUserId(adoptionId, userId);
        return favoriteOptional.map(FavoriteMapper.MAPPER::toDto).orElse(null);
    }

    @Override
    public Page<FavoriteDTO> getAll(Pageable pageable) {
        Long userId = AuthSupport.getUserId();
        Page<Favorite> favoriteList = favoriteRepository.findByUserId(userId, pageable);
        return favoriteList.map(FavoriteMapper.MAPPER::toDto);
    }

    /**
     * Obtiene adopcion, comprueba si existe.
     * @param adoptionId
     * @return
     */
    private Adoption getAdoption(Long adoptionId){
        Optional<Adoption> adoptionOptional = adoptionRepository.findById(adoptionId);
        if(adoptionOptional.isEmpty()){
            throw new BadRequestException(Error.PUBLICATION_NOT_FOUND);
        }
        return adoptionOptional.get();
    }
}
