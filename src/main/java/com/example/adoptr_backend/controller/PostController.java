package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.service.PostService;
import com.example.adoptr_backend.service.dto.request.AdoptionFilterDTO;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import com.example.adoptr_backend.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post")
@Tag(name = "Post", description = "Post Endpoints")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crea un post", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<PostDTO> createPost(@RequestParam String description,
                                              @RequestParam(required = false) MultipartFile image
    ) {
        PostDTOin dto = new PostDTOin(description, image);
        PostDTO response = postService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un post por su id", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<PostDTO> getById(@PathVariable Long id){
        PostDTO response = postService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtiene los posts de un usuario", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<List<PostDTO>> getPostsByUserId(@PathVariable Long userId, @ParameterObject Pageable pageable) {
        List<PostDTO> posts = postService.getByUserId(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtiene los posts del usuario logeado", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<List<PostDTO>> getAll(@ParameterObject Pageable pageable) {
        List<PostDTO> response = postService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/all")
    @Operation(summary = "Obtiene todos los posts", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<PostDTO>> getCommunity(@ParameterObject Pageable pageable) {
        Page<PostDTO> response = postService.getAllCommunity(pageable);
        HttpHeaders headers = PaginationUtil.setTotalCountPageHttpHeaders(response);
        return new ResponseEntity<>(response.getContent(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id)  {
        postService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}