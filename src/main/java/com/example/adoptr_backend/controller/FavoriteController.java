package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.FavoriteService;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.dto.response.FavoriteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@Tag(name = "Favorite", description = "Favorite Endpoints")
@AllArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PatchMapping("/{adoptionId}")
    @Operation(summary = "Agrega o quita de favoritos", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<FavoriteDTO> set(@PathVariable Long adoptionId){
        FavoriteDTO response = favoriteService.set(adoptionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtiene todas las publicaciones favoritas de un usuario", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<AdoptionDTO>> getAll(@ParameterObject Pageable pageable){
        List<AdoptionDTO> response = favoriteService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{adoptionId}")
    @Operation(summary = "Obtiene el favorito de una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<FavoriteDTO> get(@PathVariable Long adoptionId){
        FavoriteDTO response = favoriteService.get(adoptionId);
        return ResponseEntity.ok(response);
    }
}
