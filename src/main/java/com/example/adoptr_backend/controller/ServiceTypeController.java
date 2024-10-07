package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ServiceTypeService;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/serviceType")
@Tag(name = "ServiceType", description = "ServiceType Endpoints")

public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crea un tipo de servicio", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ServiceTypeDTO> create(@RequestParam String name,
                                                 @RequestParam String description,
                                                 @RequestParam MultipartFile image){
        ServiceTypeDTOin dto = new ServiceTypeDTOin(name, description, image);
        ServiceTypeDTO response = serviceTypeService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene los tipos de servicios, con filtro")
    public ResponseEntity<List<ServiceTypeDTO>> getAll(@ParameterObject ServiceTypeFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<ServiceTypeDTO> response = serviceTypeService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tipo de servicio por id")
    public ResponseEntity<ServiceTypeDTO> getById(@PathVariable Long id) {
        ServiceTypeDTO response = serviceTypeService.getById(id);
        return ResponseEntity.ok(response);
    }
}
