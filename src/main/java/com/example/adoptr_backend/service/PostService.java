package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    List<PostDTO> getAll(Pageable pageable);

    PostDTO create(PostDTOin dto);

    PostDTO getById(Long id);

    List<PostDTO> getByUserId(Long userId, Pageable pageable);

    void delete(Long id);

}