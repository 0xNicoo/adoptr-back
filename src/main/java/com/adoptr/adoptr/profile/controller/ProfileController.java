package com.adoptr.adoptr.profile.controller;

import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("")
    public ResponseEntity<ProfileDTO> get() {
        ProfileDTO response = profileService.get();
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDTO create(@Valid @RequestBody ProfileDTOin request) {
        return profileService.create(request);
    }

    /** Sube la foto del perfil o reemplaza la que tenía. */
    @PutMapping(path = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDTO setPhoto(@RequestPart("file") MultipartFile file) {
        return profileService.setPhoto(file);
    }

    @DeleteMapping("/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto() {
        profileService.deletePhoto();
    }
}
