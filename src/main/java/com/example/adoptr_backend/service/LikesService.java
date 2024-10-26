package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.LikesDTOin;
import com.example.adoptr_backend.service.dto.response.LikesDTO;

import java.util.List;

public interface LikesService {

    LikesDTO set(Long postId);

    LikesDTO getById(Long id);

    List<LikesDTO> getByUserId(Long userId);

    List<LikesDTO> getAll();

    List<LikesDTO> getLikesByPostId(Long postId);

    Long getPostLikesCount(Long postId);

    LikesDTO get(Long postId);
}
