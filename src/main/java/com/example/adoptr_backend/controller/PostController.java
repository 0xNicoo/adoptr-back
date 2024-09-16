package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.PostService;
import com.example.adoptr_backend.service.dto.request.PostDTOin;
import com.example.adoptr_backend.service.dto.response.PostDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@Tag(name = "Post", description = "Post Endpoints")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Crea un post", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<PostDTO> createPost(@RequestParam String description
    ) {
        PostDTOin dto = new PostDTOin(description);
        PostDTO response = postService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtiene los posts de un usuario", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<List<PostDTO>> getPostsByUserId(@PathVariable Long userId) {
        List<PostDTO> posts = postService.getByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    //TODO: paginar
    @GetMapping("/all")
    @Operation(summary = "Obtiene los posts del usuario logeado", security = {@SecurityRequirement(name = "bearer-jwt")})
    public ResponseEntity<List<PostDTO>> getAll() {
        List<PostDTO> response = postService.getAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id)  {
        postService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}