package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publication")
@Tag(name = "Publication", description = "Publication Endpoints")

public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) { this.publicationService = publicationService; }
    @GetMapping("/count")
    @Operation(summary = "Obtiene la cantidad de publicaciones", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<Long> getPublicationCount() {
        Long publicationCount = publicationService.getPublicationCount();
        return ResponseEntity.ok(publicationCount);
    }
}
