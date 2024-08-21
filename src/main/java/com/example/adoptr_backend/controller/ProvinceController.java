package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    @Operation(summary = "Obtiene provincias, con filtro")
    public ResponseEntity<List<ProvinceDTO>> getAll(@ParameterObject ProvinceFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<ProvinceDTO> response = provinceService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }
}
