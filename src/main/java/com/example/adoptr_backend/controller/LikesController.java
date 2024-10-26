package com.example.adoptr_backend.controller;
import com.example.adoptr_backend.service.LikesService;
import com.example.adoptr_backend.service.dto.response.LikesDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/like")
@Tag(name = "Like", description = "Like Endpoints")
public class LikesController {

    private final LikesService likesService;

    public LikesController(LikesService likesService) {this.likesService = likesService;}

    @PatchMapping("/{postId}")
    @Operation(summary = "Pone o quita un like", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LikesDTO> set(@PathVariable Long postId) {
        LikesDTO response = likesService.set(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un like por su id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LikesDTO> getById(@PathVariable Long id){
        LikesDTO response = likesService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Obtiene el like de un post por el post id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LikesDTO> get(@PathVariable Long postId) {
        LikesDTO response = likesService.get(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtiene los likes de un usuario", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<LikesDTO>> getLikesByUserId(@PathVariable Long userId) {
        List<LikesDTO> likes = likesService.getByUserId(userId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtiene los likes del usuario logeado", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<LikesDTO>> getAll() {
        List<LikesDTO> response = likesService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}/all")
    @Operation(summary = "Obtiene los likes de un post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<LikesDTO>> getLikesByPostId(@PathVariable Long postId) {
        List<LikesDTO> likes = likesService.getLikesByPostId(postId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "Obtiene la cantidad de likes de un post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<Long> getPostLikesCount(@PathVariable Long postId) {
        Long likesCount = likesService.getPostLikesCount(postId);
        return ResponseEntity.ok(likesCount);
    }

}
