package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;
import com.example.adoptr_backend.service.LostService;
import com.example.adoptr_backend.service.dto.request.AdoptionStatusDTOin;
import com.example.adoptr_backend.service.dto.request.LostDTOin;
import com.example.adoptr_backend.service.dto.request.LostFilterDTO;
import com.example.adoptr_backend.service.dto.request.LostStatusDTOin;
import com.example.adoptr_backend.service.dto.response.LostDTO;
import com.example.adoptr_backend.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lost")
@Tag(name = "Lost", description = "Lost Endpoints")
public class LostController {

    private final LostService lostService;

    public LostController(LostService lostService) {this.lostService = lostService;}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "crea una publicacion de perdidos", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LostDTO> create(@RequestParam String title,
                                          @RequestParam String description,
                                          @RequestParam SexType sexType,
                                          @RequestParam AnimalType animalType,
                                          @RequestParam SizeType sizeType,
                                          @RequestParam int ageYears,
                                          @RequestParam int ageMonths,
                                          @RequestParam double longitude,
                                          @RequestParam double latitude,
                                          @RequestParam MultipartFile image,
                                          @RequestParam Long locality_id){
        LostDTOin dto = new LostDTOin(title, description, sexType, animalType, sizeType, ageYears, ageMonths, longitude, latitude, image, locality_id);
        LostDTO response =  lostService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene publicaciones de perdidos, con filtro", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<LostDTO>> getAll(@ParameterObject LostFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<LostDTO> response = lostService.getAll(filterDTO, pageable);
        HttpHeaders headers = PaginationUtil.setTotalCountPageHttpHeaders(response);
        return new ResponseEntity<>(response.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una publicacionde perdidos por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LostDTO> getById(@PathVariable Long id) {
        LostDTO response = lostService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifica una publicacion de perdidos", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<LostDTO> update(@PathVariable Long id,
                                              @RequestParam String title,
                                              @RequestParam String description,
                                              @RequestParam SexType sexType,
                                              @RequestParam(required = false) AnimalType animalType,
                                              @RequestParam SizeType sizeType,
                                              @RequestParam int ageYears,
                                              @RequestParam int ageMonths,
                                              @RequestParam double longitude,
                                              @RequestParam double latitude,
                                              @RequestParam(required = false) MultipartFile image,
                                              @RequestParam Long locality_id){
        LostDTOin dto = new LostDTOin(title, description, sexType, animalType, sizeType, ageYears, ageMonths, longitude, latitude, image, locality_id);
        LostDTO response = lostService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una publicacion de perdidos", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id)  {
        lostService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/status")
    @Operation(summary = "Cambia estado de una adopcion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> changeStatus(@RequestBody LostStatusDTOin dtoIn){
        lostService.changeStatus(dtoIn);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
