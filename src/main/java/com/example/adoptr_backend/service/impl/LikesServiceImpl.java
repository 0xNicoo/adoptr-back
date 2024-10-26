package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Likes;
import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.LikesRepository;
import com.example.adoptr_backend.repository.PostRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.LikesService;
import com.example.adoptr_backend.service.dto.request.LikesDTOin;
import com.example.adoptr_backend.service.dto.response.LikesDTO;
import com.example.adoptr_backend.service.mapper.LikesMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class LikesServiceImpl implements LikesService {
    private final LikesRepository likesRepository;
    private final UserRepository userRepository;

    private final PostRepository postRepository;

    public LikesServiceImpl(LikesRepository likesRepository,
                            UserRepository userRepository,
                            PostRepository postRepository
    ) {
        this.likesRepository = likesRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    @Override
    public LikesDTO set(Long postId) {
        Long userId = AuthSupport.getUserId();
        User user = getUserById(userId);
        Post post = getPostById(postId);
        Optional<Likes> likesOptional = likesRepository.findByUser_IdAndPost_Id(userId, postId);
        if(likesOptional.isEmpty()) {
            Likes likes = new Likes();
            likes.setDate(LocalDateTime.now());
            likes.setUser(user);
            likes.setPost(post);
            likes = likesRepository.save(likes);
            return mapLikeToDTO(likes);
        }
        likesRepository.delete(likesOptional.get());
        return null;
    }

    @Override
    public LikesDTO getById(Long id) {
        Likes likes = getLike(id);
        return mapLikeToDTO(likes);
    }

    @Override
    public LikesDTO get(Long postId) {
        Long userId = AuthSupport.getUserId();
        Optional<Likes> likesOptional = likesRepository.findByUser_IdAndPost_Id(userId, postId);
        return likesOptional.map(LikesMapper.MAPPER::toDto).orElse(null);
    }

    @Override
    public List<LikesDTO> getByUserId(Long userId) {
        List<Likes> likes = likesRepository.findByUserId(userId);
        return likes.stream()
                .map(this::mapLikeToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LikesDTO> getAll() {
        Long userId = AuthSupport.getUserId();
        List<Likes> likeList = likesRepository.findByUserId(userId);
        return likeList.stream()
                .map(this::mapLikeToDTO)
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new BadRequestException(Error.USER_NOT_FOUND);
        }
        return userOptional.get();
    }

    private Post getPostById(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isEmpty()) {
            throw new BadRequestException(Error.POST_NOT_FOUND);
        }
        return postOptional.get();
    }
    private LikesDTO mapLikeToDTO(Likes like) {
        return LikesMapper.MAPPER.toDto(like);
    }

    @Override
    public Long getPostLikesCount(Long postId) {
        return likesRepository.countByPostId(postId);
    }

    @Override
    public List<LikesDTO> getLikesByPostId(Long postId) {
        List<Likes> likesList = likesRepository.findByPostId(postId);
        return likesList.stream()
                .map(this::mapLikeToDTO)
                .collect(Collectors.toList());
    }

    private Likes getLike(Long id) {
        Optional<Likes> likeOptional = likesRepository.findById(id);
        return likeOptional.get();
    }

}
