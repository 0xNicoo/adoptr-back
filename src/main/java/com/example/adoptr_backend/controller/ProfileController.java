package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile", description = "Profile Endpoints")

public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crea un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> create(@ModelAttribute ProfileDTOin dto){
        ProfileDTO response =  profileService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un perfil por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> getById(@PathVariable Long id) {
        ProfileDTO response = profileService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifica un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> update(@PathVariable Long id, @ModelAttribute ProfileDTOin dto) {
        ProfileDTO response = profileService.update(id, dto);
        return ResponseEntity.ok(response);
    }
}
