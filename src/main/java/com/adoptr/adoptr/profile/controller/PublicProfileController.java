package com.adoptr.adoptr.profile.controller;

import com.adoptr.adoptr.profile.dto.response.PublicProfileDTO;
import com.adoptr.adoptr.profile.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfiles de cualquier usuario, públicos. El perfil propio y su edición están en {@code /profile}. */
@RestController
@RequestMapping("/profiles")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{id}")
    public PublicProfileDTO get(@PathVariable Long id) {
        return profileService.getPublic(id);
    }
}
