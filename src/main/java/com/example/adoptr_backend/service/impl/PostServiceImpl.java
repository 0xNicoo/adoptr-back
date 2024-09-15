package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.PostRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.PostService;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import com.example.adoptr_backend.service.mapper.PostMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public PostDTO create(PostDTOin dto) {
        Long userId = AuthSupport.getUserId();
        User user = getUserById(userId);

        Post post = new Post();
        post.setDescription(dto.getDescription());
        post.setDate(LocalDateTime.now());
        post.setUser(user);

        post = postRepository.save(post);

        return mapPostToDTO(post);
    }

    @Override
    public List<PostDTO> getByUserId(Long userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream()
                .map(this::mapPostToDTO)
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new BadRequestException(Error.USER_NOT_FOUND);
        }
        return userOptional.get();
    }

    private PostDTO mapPostToDTO(Post post) {
        PostDTO dto = PostMapper.MAPPER.toDto(post);
        return dto;
    }
}