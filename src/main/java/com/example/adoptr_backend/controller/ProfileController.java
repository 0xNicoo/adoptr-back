package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.model.GenderType;
import com.example.adoptr_backend.service.ProfileService;
import com.example.adoptr_backend.service.dto.request.ProfileDTOin;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ProfileDTO> create(@RequestParam String firstName,
                                             @RequestParam String lastName,
                                             @RequestParam GenderType genderType,
                                             @RequestParam String description,
                                             @RequestParam Long locality_id,
                                             @RequestParam MultipartFile image){
        ProfileDTOin dto = new ProfileDTOin(firstName, lastName, genderType, description, locality_id, image);
        ProfileDTO response =  profileService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un perfil por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> getById(@PathVariable Long id) {
        ProfileDTO response = profileService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userid}")
    @Operation(summary = "Obtiene un perfil por user id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> getByUserId(@PathVariable Long userid) {
        ProfileDTO response = profileService.getByUserId(userid);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene el perfil del usuario logeado", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> get() {
        ProfileDTO response = profileService.get();
        return ResponseEntity.ok(response);
    }

    //TODO: permitir que se pasen parametros vacios sin que se carguen como NULL
    @PutMapping(path = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifica un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ProfileDTO> update(@PathVariable Long id,
                                             @RequestParam String firstName,
                                             @RequestParam String lastName,
                                             @RequestParam GenderType genderType,
                                             @RequestParam String description,
                                             @RequestParam Long locality_id,
                                             @RequestParam(required = false) MultipartFile image) {
        ProfileDTOin dto = new ProfileDTOin(firstName, lastName, genderType, description, locality_id, image);
        ProfileDTO response = profileService.update(id, dto);
        return ResponseEntity.ok(response);
    }
}
