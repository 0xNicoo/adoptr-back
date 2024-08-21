package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.AdoptionService;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.AdoptionFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoption")
@Tag(name = "Adoption", description = "Adoption Endpoints")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {this.adoptionService = adoptionService;}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "crea una publicacion de adopcion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<AdoptionDTO> create(@ModelAttribute AdoptionDTOin dto){
        AdoptionDTO response =  adoptionService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene publicaciones de adopcion, con filtro", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<AdoptionDTO>> getAll(@ParameterObject AdoptionFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<AdoptionDTO> response = adoptionService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una publicacionde adopcion por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<AdoptionDTO> getById(@PathVariable Long id) {
        AdoptionDTO response = adoptionService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifica una publicacion de adopcion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<AdoptionDTO> update(@PathVariable Long id, @ModelAttribute AdoptionDTOin dto) {
        AdoptionDTO response = adoptionService.update(id, dto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una publicacion de adopcion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id)  {
        adoptionService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

