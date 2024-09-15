package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;

import java.util.List;

public interface PostService {

    PostDTO create(PostDTOin dto);

    List<PostDTO> getByUserId(Long userId);

}