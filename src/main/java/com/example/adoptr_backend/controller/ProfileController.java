package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile", description = "Profile Endpoints")

public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @PostMapping
    @Operation(summary = "Crea un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> create(@RequestBody ProfileDTOin dto){
        ProfileDTO response =  profileService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un perfil por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> getById(@PathVariable Long id) {
        ProfileDTO response = profileService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> update(@PathVariable Long id, @RequestBody ProfileDTOin dto) {
        ProfileDTO response = profileService.update(id, dto);
        return ResponseEntity.ok(response);
    }
}
