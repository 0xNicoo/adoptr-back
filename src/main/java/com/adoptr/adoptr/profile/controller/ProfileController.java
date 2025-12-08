package com.adoptr.adoptr.profile.controller;

import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProfileDTO> create(@RequestBody ProfileDTOin request) {
        return ResponseEntity.ok(null);
    }
}
