package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.LocalityService;
import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.dto.request.LocalityDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.response.LocalityDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/locality")
@Tag(name = "Locality", description = "Locality Endpoints")
public class LocalityController {

    private final LocalityService localityService;

    public LocalityController(LocalityService localityService) {
        this.localityService = localityService;
    }

    @PostMapping
    @Operation(summary = "Crea una localidad")
    public ResponseEntity<LocalityDTO> create(@RequestBody LocalityDTOin dto){
        LocalityDTO response =  localityService.create(dto);
        return ResponseEntity.ok(response);
    }
}
