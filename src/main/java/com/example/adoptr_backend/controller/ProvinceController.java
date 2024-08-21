package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/province")
@Tag(name = "Province", description = "Province Endpoints")
public class ProvinceController {

    private final ProvinceService provinceService;

    public ProvinceController(ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @PostMapping
    @Operation(summary = "Crea una provincia")
    public ResponseEntity<ProvinceDTO> create(@RequestBody ProvinceDTOin dto){
        ProvinceDTO response =  provinceService.create(dto);
        return ResponseEntity.ok(response);
    }
}
