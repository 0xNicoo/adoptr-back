package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.ImageType;
import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.PostRepository;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.PostService;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import com.example.adoptr_backend.service.mapper.PostMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final ImageService imageService;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           ImageService imageService
    ) {
        this.imageService = imageService;
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

        if (dto.getImage() != null) {
            Long imageId = imageService.uploadImage(dto.getImage(), ImageType.NEWS, post.getId());
            post.setImageId(imageId);
            post = postRepository.save(post);
        }

        return mapPostToDTO(post);
    }

    @Override
    public PostDTO getById(Long id) {
        Post post = getPost(id);
        return mapPostToDTO(post);
    }

    @Override
    public List<PostDTO> getByUserId(Long userId, Pageable pageable) {
        List<Post> posts = postRepository.findByUserIdOrderByIdDesc(userId, pageable);
        return posts.stream()
                .map(this::mapPostToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getAll(Pageable pageable) {
        Long userId = AuthSupport.getUserId();
        List<Post> postList = postRepository.findByUserIdOrderByIdDesc(userId, pageable);
        return postList.stream()
                .map(this::mapPostToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PostDTO> getAllCommunity(Pageable pageable) {
        Page<Post> page = postRepository.findAllByOrderByDateDesc(pageable);
        Page<PostDTO> dtoPage = page.map(post -> {
            try {
                PostDTO dto = PostMapper.MAPPER.toDto(post);
                String s3Url = imageService.getS3url(post.getId(), ImageType.NEWS);
                dto.setS3Url(s3Url);
                return dto;
            } catch (Exception e) {
                System.err.println("Error al obtener la URL de la imagen para ID " + post.getId() + ": " + e.getMessage());
                PostDTO dto = PostMapper.MAPPER.toDto(post);
                dto.setS3Url(null);
                return dto;
            }
        });
        return dtoPage;
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
        if (post.getImageId() != null) {
            String s3Url = imageService.getS3url(post.getId(), ImageType.NEWS);
            dto.setS3Url(s3Url);
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        Long userId = AuthSupport.getUserId();
        Post post = getPost(id);
        if (!Objects.equals(post.getUser().getId(), userId)) {
            throw new BadRequestException(Error.USER_NOT_POST_OWNER);
        }
        if (post.getImageId() != null) {
            imageService.deleteImage(post.getId(), ImageType.NEWS);
        }
        postRepository.delete(post);
    }

    private Post getPost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        return postOptional.get();
    }

}