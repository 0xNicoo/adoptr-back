package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.ServiceTypeService;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import com.example.adoptr_backend.service.dto.request.ServiceTypeDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceTypeFilterDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import com.example.adoptr_backend.service.dto.response.ServiceTypeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/serviceType")
@Tag(name = "ServiceType", description = "ServiceType Endpoints")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @PostMapping
    @Operation(summary = "Crea un tipo de servicio")
    public ResponseEntity<ServiceTypeDTO> create(@RequestBody ServiceTypeDTOin dto){
        ServiceTypeDTO response =  serviceTypeService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene los tipos de servicios, con filtro")
    public ResponseEntity<List<ServiceTypeDTO>> getAll(@ParameterObject ServiceTypeFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<ServiceTypeDTO> response = serviceTypeService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }
}
